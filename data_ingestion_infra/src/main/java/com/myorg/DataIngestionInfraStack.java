package com.myorg;

import java.util.Arrays;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionUrl;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.FunctionUrlAuthType;
import software.amazon.awscdk.services.lambda.FunctionUrlOptions;
import software.amazon.awscdk.services.stepfunctions.Chain;
import software.amazon.awscdk.services.stepfunctions.StateMachine;
import software.amazon.awscdk.services.stepfunctions.Choice;
import software.amazon.awscdk.services.stepfunctions.Condition;
import software.amazon.awscdk.services.stepfunctions.Pass;
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke;

public class DataIngestionInfraStack extends Stack {
    public final FunctionUrl func_url;
    public final Function entry_function;

    public DataIngestionInfraStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public DataIngestionInfraStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        final Bucket project_bucket = Bucket.Builder.create(this, "Project Bucket").build();

        final Table db_tb = Table.Builder.create(this, "UserInfoTable").partitionKey(GetUserTableKey("id")).build();

        final Role lambda_ex_role = Role.Builder.create(this, "LambdaExecutionRole")
                .assumedBy(ServicePrincipal.Builder.create("lambda.amazonaws.com")
                        .build())
                .managedPolicies(Arrays.asList(
                        ManagedPolicy.fromAwsManagedPolicyName("AWSLambdaExecute"),
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonDynamoDBFullAccess"),
                        ManagedPolicy.fromAwsManagedPolicyName("AWSStepFunctionsFullAccess")))
                .build();

        final ManagedPolicy sm_access_policy = ManagedPolicy.Builder.create(this, "SecretsManagerAccessPolicy")
                .statements(Arrays.asList(
                        GetPolicyStatementSecretsManager()))
                .build();

        lambda_ex_role.addManagedPolicy(sm_access_policy);

        final Role stepfunction_ex_role = Role.Builder.create(this, "StepFucntionExecutionRole")
                .assumedBy(ServicePrincipal.Builder.create("states.amazonaws.com")
                        .build())
                .managedPolicies(Arrays.asList(
                        ManagedPolicy.fromAwsManagedPolicyName("AWSStepFunctionsFullAccess"),
                        ManagedPolicy.fromAwsManagedPolicyName("AWSLambda_FullAccess")))
                .build();

        final Function trigger_wf = Function.Builder.create(this, "Trigger Workflow")
                .runtime(Runtime.PYTHON_3_8)
                .handler("handler.lambda_handler")
                .code(Code.fromAsset("../lambda/triggerWorkflow/"))
                .role(lambda_ex_role)
                .build();

        this.func_url = trigger_wf.addFunctionUrl(GetURLAuthAttr());
        this.entry_function = trigger_wf;

        final Function check_email_lf = Function.Builder.create(this, "Check-Email")
                .runtime(Runtime.PYTHON_3_8)
                .handler("handler.lambda_handler")
                .code(Code.fromAsset("../lambda/check-email/"))
                .role(lambda_ex_role)
                .build();

        final Function check_pincode_lf = Function.Builder.create(this, "Check-Pincode")
                .runtime(Runtime.PYTHON_3_8)
                .handler("handler.lambda_handler")
                .code(Code.fromAsset("../lambda/check-pincode/"))
                .role(lambda_ex_role)
                .build();

        final Function addToDB_lf = Function.Builder.create(this, "AddToDB")
                .runtime(Runtime.PYTHON_3_8)
                .handler("handler.lambda_handler")
                .code(Code.fromAsset("../lambda/addToDb/"))
                .role(lambda_ex_role)
                .build();
        addToDB_lf.addEnvironment("TABLE", db_tb.getTableName());

        final LambdaInvoke check_email_state = LambdaInvoke.Builder.create(this, "CheckEmail")
                .lambdaFunction(check_email_lf)
                .outputPath("$.Payload")
                .build();
        
        final LambdaInvoke check_pincode_state = LambdaInvoke.Builder.create(this, "CheckPincode")
                .lambdaFunction(check_pincode_lf)
                .outputPath("$.Payload")
                .inputPath("$.body")
                .build();
        
        final LambdaInvoke add_to_db_state = LambdaInvoke.Builder.create(this, "AddToDBState")
                .lambdaFunction(addToDB_lf)
                .outputPath("$.Payload")
                .inputPath("$.body")
                .build();
        
        final Pass fail_state_1 = Pass.Builder.create(this, "Fail Email Check").build();
        final Pass fail_state_2 = Pass.Builder.create(this, "Fail Pincode Check").build();

        final Choice choice_email = new Choice(this, "Email Check")
            .when(Condition.numberEquals("$.statusCode", 400), fail_state_1)
            .otherwise(check_pincode_state);
        
        final Choice choice_pincode = new Choice(this, "Pincode Check")
            .when(Condition.numberEquals("$.statusCode", 400), fail_state_2)
            .otherwise(add_to_db_state);

        Chain chain2 = Chain.start(check_pincode_state).next(choice_pincode);

        Chain chain = Chain.start(check_email_state)
            .next(choice_email);
        

        final StateMachine workflow = StateMachine.Builder.create(this, "Workflow")
            .definition(chain)
            .role(stepfunction_ex_role)
            .build();
        
        trigger_wf.addEnvironment("STEP_FUNCTION", workflow.getStateMachineArn());
        trigger_wf.addEnvironment("TABLE", db_tb.getTableName());

        CfnOutput.Builder.create(this, "Function URL API")
            .value(this.func_url.getUrl())
            .build();
        
        CfnOutput.Builder.create(this, "ProjectBucketOP")
            .value(project_bucket.getBucketName())
            .build();       

    }

    private Attribute GetUserTableKey(String keyname) {
        return Attribute.builder().name(keyname).type(AttributeType.STRING).build();
    }

    private PolicyStatement GetPolicyStatementSecretsManager() {
        return PolicyStatement.Builder
                .create()
                .resources(Arrays.asList("arn:aws:secretsmanager:*:*:secret:*data-ingestion*"))
                .actions(Arrays.asList("secretsmanager:GetSecretValue", "secretsmanager:DescribeSecret"))
                .effect(Effect.ALLOW)
                .build();
    }

    private FunctionUrlOptions GetURLAuthAttr() {
        return FunctionUrlOptions.builder()
                .authType(FunctionUrlAuthType.NONE)
                .build();
    }
}
