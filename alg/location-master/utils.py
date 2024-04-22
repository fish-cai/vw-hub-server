# some utils
import math
import os
import shutil
import re
import pandas as pd

# 设置pandas的显示长度，None代表无限制
pd.set_option('display.max_rows', None)
pd.set_option('display.max_columns', None)

# 设置显示宽度，-1代表自动检测显示宽度
pd.set_option('display.width', None)

# 设置显示列的宽度，None代表自动
pd.set_option('display.max_colwidth', None)


def save_to_file(file_name, contents):
    fh = open(file_name, 'w')
    fh.write(contents)
    fh.close()


def calculate_train_transportation_time(volume, distance):
    if volume <= 49:
        transportation_time = math.ceil(distance / 300) + 1
    elif volume <= 149:
        transportation_time = math.ceil(distance / 350) + 1
    elif volume <= 289:
        transportation_time = math.ceil(distance / 400) + 1
    else:
        transportation_time = math.ceil(distance / 500) + 1

    return transportation_time


def calculate_cost_coef(trans_cost, distance):
    cost = 0
    for i, row in trans_cost.iterrows():
        if row['范围起（≥）'] <= distance < row['范围止（＜）']:
            if i == 0:
                return trans_cost.loc[0, "总费用"]
            cost = previous_cost + (distance - row['范围起（≥）']) * row['公路费率']
        # 边际成本
        previous_cost = row['边际成本']

    return cost


# 干线运输结果处理
def main_trans_result(data, z, x, w):
    city_installed = list()
    # print(" ================= 干线成本相关，以及Hub城市 =================")
    for i, h in enumerate(data.train_hubs["城市"]):
        if z[(h, "train")].solution_value() > 0:
            distance = int(data.distance_matrix.get("合肥").get(h, 99) * 10000) / 10000.0
            # hub 总销量
            total_hub_volume = calculate_hub_covered_volume(data, h, "train", x)
            volume_revised = total_hub_volume / 300 * 3
            prepare_time = 3
            transportation_time = calculate_train_transportation_time(volume_revised, distance)

            # hub 总成本
            # print(w[(h, "train")].solution_value(),  total_hub_volume)
            cost = int(data.city_location.loc[h, "合肥出发铁路干线"] * total_hub_volume * 10000) / 10000.0
            h_province = data.city_location["省份"][h]
            city_installed.append([h, h_province, "铁路运输", distance, cost, prepare_time, transportation_time])

    for i, h in enumerate(data.ship_hubs["城市"]):
        if z[(h, "ship")].solution_value() > 0:
            distance = int(data.distance_matrix.get("合肥").get(h, 99) * 10000) / 10000.0
            prepare_time = 3
            transportation_time = math.ceil(distance / 500) + 1

            # hub 总销量
            total_hub_volume = calculate_hub_covered_volume(data, h, "ship", x)
            cost = int(data.city_location.loc[h, "合肥出发水路干线"] * total_hub_volume * 10000) / 10000.0
            h_province = data.city_location["省份"][h]
            city_installed.append([h, h_province, "水路运输", distance, cost, prepare_time, transportation_time])
    # 直发占位用
    city_installed.append(["合肥", "安徽", "公路直发", 0, 0, 0, 0])
    # 输出被选中的城市
    city_installed_df = pd.DataFrame(city_installed,
                                     columns=['中转库城市', "中转库省份", '干线运输方式', '干线距离',
                                              "干线（覆盖城市）总成本",
                                              '干线准备时间',
                                              '干线运输时间'])
    # print(city_installed_df)
    return city_installed_df


def calculate_hub_covered_volume(data, h, hub_type, x):
    covered_volume = 0
    for j, c in enumerate(data.city_location.index):
        if x[(h, c, hub_type)].solution_value() > 0:
            covered_volume += data.city_location.loc[c, "销量"]

    return covered_volume


# 自定义函数用于聚合计算
def custom_agg_function(group):
    # 计算每个组的总销量、总时间
    sales_sum = group['销量'].sum()
    time_sum = group['单台时间'].sum()
    cost_sum = group['总成本/￥'].sum()
    # 计算 sum(销量*时间)
    sales_time_product = (group['销量'] * group['单台时间']).sum()
    # 计算总成本
    cost_sum_product = (group['销量'] * group['单台时间']).sum()
    # 计算单台成本
    unit_cost = cost_sum / sales_sum
    # 计算单位时间
    unit_time = sales_time_product / sales_sum

    return pd.Series({
        '中转库城市': group['中转库城市'].iloc[0],
        '中转库省份': group['中转库省份'].iloc[0],
        '干线运输方式': group['干线运输方式'].iloc[0],
        '总销量': sales_sum,
        '总时间': time_sum,
        '总成本': cost_sum,
        'sum(销量*时间）': sales_time_product,
        '单台成本': unit_cost,
        '单台时间': unit_time
    })

    # 按中转库城市、中转库省份和干线运输方式分组并聚合数据
    grouped_df = group.groupby(['中转库城市', '中转库省份', '干线运输方式']).apply(custom_agg_function)
    return grouped_df


def clear_directory(folder_path):
    # 检查文件夹是否存在
    if os.path.exists(folder_path):
        # 获取文件夹内所有文件和文件夹的名字
        for filename in os.listdir(folder_path):
            # 拼接完整的文件或文件夹路径
            file_path = os.path.join(folder_path, filename)
            try:
                # 如果是文件夹，则递归删除
                if os.path.isdir(file_path):
                    shutil.rmtree(file_path)
                # 如果是文件，则直接删除
                else:
                    os.unlink(file_path)
            except Exception as e:
                print(f'删除失败。错误信息: {e}')
    else:
        print("指定的文件夹不存在。")


#    file_out_dir 下有很多xlsx文件,我需要解析所有文件的文件名，取其中数字进行升序排列

def rename_files_and_select_top_20(file_out_dir):
    # 获取文件夹中所有的xlsx文件名
    file_names = [f for f in os.listdir(file_out_dir) if f.endswith('.xlsx')]

    # 提取文件名中的数字，并与原文件名一起存储
    files_with_numbers = []
    numbers_seen = set()  # 用于记录已经出现过的数字
    for file_name in file_names:
        # 使用正则表达式提取文件名中的数字
        numbers = re.findall(r'\d+', file_name)
        if numbers:
            # 假设每个文件名中只有一个数字
            number = int(numbers[0])
            if number not in numbers_seen:
                files_with_numbers.append((file_name, number))
                numbers_seen.add(number)
            else:
                # 如果数字已经出现过，则删除该文件
                os.remove(os.path.join(file_out_dir, file_name))
                print(f'文件 {file_name} 因编号重复已被删除')

    # 按数字升序排序
    files_with_numbers.sort(key=lambda x: x[1])

    # 重命名所有文件
    for i, (original_file_name, _) in enumerate(files_with_numbers, start=1):
        # 构造新的文件名
        new_file_name = f'Temp_Location_Output_{i:03}.xlsx'
        # 构造原文件和新文件的完整路径
        original_file_path = os.path.join(file_out_dir, original_file_name)
        new_file_path = os.path.join(file_out_dir, new_file_name)
        # 重命名文件
        os.rename(original_file_path, new_file_path)
        print(f'文件 {original_file_name} 已临时重命名为 {new_file_name}')

    # 重新获取重命名后的所有文件名
    renamed_file_names = [f for f in os.listdir(file_out_dir) if f.startswith('Temp_Location_Output_')]

    # 仅保留TOP 20的文件，删除其他文件
    for file_name in renamed_file_names[20:]:
        os.remove(os.path.join(file_out_dir, file_name))
        print(f'文件 {file_name} 超出TOP 20范围，已被删除')

    # 对TOP 20的文件进行最终重命名
    for i, file_name in enumerate(renamed_file_names[:20], start=1):
        # 构造新的文件名
        new_file_name = f'Location_Output_{i:02}.xlsx'
        # 构造原文件和新文件的完整路径
        original_file_path = os.path.join(file_out_dir, file_name)
        new_file_path = os.path.join(file_out_dir, new_file_name)
        # 重命名文件
        os.rename(original_file_path, new_file_path)
        print(f'文件 {file_name} 已最终重命名为 {new_file_name}')


