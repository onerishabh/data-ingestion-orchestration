import json
import boto3
import os

def lambda_handler(event, context):
    if "queryStringParameters" in event:
        params = event['queryStringParameters']
        
        if 'user_name' not in params or 'email' not in params or 'pincode' not in params:
            return{
                "statusCode": 400,
                "body" : json.dumps("invalid input")
            }
    
    
        client = boto3.client("stepfunctions")
        
        response = client.start_execution(
            stateMachineArn=os.environ["STEP_FUNCTION"],
            input=json.dumps(params)
        )
        return {
            'statusCode': 200,
            'body': json.dumps(f"{params['user_name']} is added to database.")
    }
    else:
        dynamo_client = boto3.client("dynamodb")
        op=[]

        for item in scan_table(dynamo_client, TableName=os.environ["TABLE"]):
            op.append(item)
        
        res = []
        for item in op:
            i = {
                'id' : item['id']['S'], 
                'user_name' : item['user_name']['S'], 
                'pincode' : item['pincode']['S'], 
            }
            res.append(i)
        return{
            'statusCode': 200,
            'body': json.dumps(res)
        }


def scan_table(dynamo_client, *, TableName, **kwargs):
    paginator = dynamo_client.get_paginator("scan")

    for page in paginator.paginate(TableName=TableName, **kwargs):
        yield from page["Items"]