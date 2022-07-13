package com.myorg;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.cloudwatch.Dashboard;
import software.amazon.awscdk.services.cloudwatch.GraphWidget;
import software.amazon.awscdk.services.cloudwatch.Metric;
import software.amazon.awscdk.services.cloudwatch.MetricOptions;
import software.amazon.awscdk.services.cloudwatch.MetricProps;
import software.amazon.awscdk.services.cloudwatch.GraphWidgetProps;
import software.amazon.awscdk.services.cloudwatch.Row;
import software.amazon.awscdk.services.cloudwatch.GraphWidgetView;
import software.amazon.awscdk.services.cloudwatch.PeriodOverride;
import software.constructs.Construct;

public class MonitorLoadStack extends Stack {
    private final Metric invocations = new Metric(
            MetricProps.builder()
                    .namespace("AWS/Lambda")
                    .metricName("Invocations")
                    .statistic("sum")
                    .build());
    private final Metric durationMetric_min = new Metric(
            MetricProps.builder()
                    .namespace("AWS/Lambda")
                    .metricName("Duration")
                    .statistic("Minimum")
                    .label("Min")
                    .color("#0000FF")
                    .build());
    private final Metric durationMetric_max = new Metric(
            MetricProps.builder()
                    .namespace("AWS/Lambda")
                    .metricName("Duration")
                    .statistic("Maximum")
                    .label("Max")
                    .color("#00FF00")
                    .build());
    private final Metric durationMetric_avg = new Metric(
            MetricProps.builder()
                    .namespace("AWS/Lambda")
                    .metricName("Duration")
                    .statistic("Average")
                    .label("Avg")
                    .color("#FFA500")
                    .build());

    private final Metric concurrentLambdaMetric = new Metric(
            MetricProps.builder()
                    .namespace("AWS/Lambda")
                    .metricName("ConcurrentExecutions")
                    .statistic("Maximum")
                    .build());

    private final Metric billingMetric = new Metric(
            MetricProps.builder()
                    .namespace("AWS/Billing")
                    .metricName("EstimatedCharges")
                    .statistic("Maximum")
                    .period(Duration.minutes(360))
                    .build());

    public MonitorLoadStack(final Construct scope, final String id) {
        this(scope, id, null, null);
    }

    public MonitorLoadStack(final Construct scope, final String id, final StackProps props, String entry_func_name) {
        super(scope, id, props);

        Map<String, String> lambda_dimension = new HashMap<String, String>();
        lambda_dimension.put("FunctionName", entry_func_name);

        final GraphWidget widget_1 = new GraphWidget(
                GraphWidgetProps.builder()
                        .title("Lambda Invocation")
                        .height(6)
                        .width(8)
                        .left(Arrays.asList(
                                invocations.with(MetricOptions.builder()
                                        .dimensionsMap(lambda_dimension)
                                        .build())))
                        .region("us-east-1")
                        .build());

        final GraphWidget widget_2 = new GraphWidget(
                GraphWidgetProps.builder()
                        .title("Lambda Duration")
                        .height(6)
                        .width(8)
                        .left(Arrays.asList(
                                durationMetric_min.with(MetricOptions.builder()
                                        .dimensionsMap(lambda_dimension)
                                        .build()),
                                durationMetric_max.with(MetricOptions.builder()
                                        .dimensionsMap(lambda_dimension)
                                        .build()),
                                durationMetric_avg.with(MetricOptions.builder()
                                        .dimensionsMap(lambda_dimension)
                                        .build())))
                        .region("us-east-1")
                        .build());

        final GraphWidget widget_3 = new GraphWidget(
                GraphWidgetProps.builder()
                        .title("Error count and success rate (%)")
                        .height(6)
                        .width(8)
                        .left(Arrays.asList(
                                concurrentLambdaMetric.with(MetricOptions.builder()
                                        .dimensionsMap(lambda_dimension)
                                        .build())))
                        .region("us-east-1")
                        .build());

        Map<String, String> currency_dimension = new HashMap<String, String>();
        currency_dimension.put("Currency", "USD");

        final GraphWidget widget_4 = new GraphWidget(
                GraphWidgetProps.builder()
                        .title("Estimated Charges (USD)")
                        .height(6)
                        .width(8)
                        .view(GraphWidgetView.BAR)
                        .left(Arrays.asList(
                                billingMetric.with(MetricOptions.builder()
                                        .dimensionsMap(currency_dimension)
                                        .build())))
                        .region("us-east-1")
                        .build());

        final Dashboard monitor_traffic_dsh = Dashboard.Builder.create(this, "MonitorTrafficDash")
                .dashboardName("Traffic_Monitor_Dashboard")
                .periodOverride(PeriodOverride.AUTO)
                .widgets(
                        Arrays.asList(
                                Arrays.asList(new Row(widget_1, widget_2, widget_3)),
                                Arrays.asList(new Row(widget_4))))
                .build();

    }
}
