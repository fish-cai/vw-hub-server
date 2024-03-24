import pandas as pd


def group_data(df):
    print(df.columns)
    # 按中转库城市、中转库省份和干线运输方式分组并聚合数据
    grouped_df = df.groupby(['中转库城市', '中转库省份', '干线运输方式']).agg(
        cost_sum=('总成本/￥', sum),
        sales_sum=('销量', sum),
        time_sum=('单台时间', sum),
    )
    grouped_df.columns
    # 计算每个组的总销量、总时间、sum(销量*时间）、单台成本和单位时间
    sales_sum_1 = grouped_df['sales_sum'] * grouped_df['time_sum']
    unit_time = sales_sum_1 / grouped_df['time_sum']
    unit_cost = grouped_df['cost_sum'] / grouped_df['sales_sum']

    # 返回一个包含这些值的新的 DataFrame
    new_df = pd.DataFrame({
        '中转库城市': grouped_df.index.get_level_values(0),
        '中转库省份': grouped_df.index.get_level_values(1),
        '干线运输方式': grouped_df.index.get_level_values(2),
        '总销量': grouped_df['sales_sum'],
        '总时间': grouped_df['time_sum'],
        'sum(销量*时间）': sales_sum_1,
        '单台成本': unit_cost,
        "单台时间": unit_time,
    })
    return new_df

# 示例用法
df = pd.DataFrame({
    '中转库城市': ['北京', '上海', '广州', '深圳', '杭州', '南京'],
    '中转库省份': ['北京', '上海', '广东', '广东', '浙江', '江苏'],
    '干线运输方式': ['公路', '铁路', '公路', '铁路', '公路', '铁路'],
    '总成本/￥': [1000, 2000, 3000, 4000, 5000, 6000],
    '销量': [100, 200, 300, 400, 500, 600],
    '单台时间': [10, 20, 30, 40, 50, 60]
})



new_df = group_data(df)

print(new_df.to_string())
