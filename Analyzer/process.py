import csv

### ********** Set values here **********
# dataset
sensor_file_name = 'data/output0.csv'
origin_file_name = 'data/timestamp0.csv'
rgb_file_name = 'data/RGBImage0.csv' # put this only for galaxy
output_file_name = 'data/result(Galaxy)0.csv'

# variables
mode = 0 # 0 for galaxy, 1 for iphone
debug = 0 # 0 for without log output, 1 for test log output
midnight = 0 # set 1 if data include midnight (00:00 AM)

interval = 7 # sec / color
color_num = 625


### start process #####################
rgb_data = []
sensor_data = []
origin_data = []

def add_time(time):
        time[2] += interval
        if time[2] >= 60:
                time[2] -= 60
                time[1] += 1
        if time[1] >= 60:
                time[1] -= 60
                time[0] += 1

        return time
		
def time_to_sec(time):
        return ((time[0] * 60) + time[1]) * 60 + time[2]

def is_same_rgb(time, data):
        time_sec = time_to_sec(time)
        data_origin = ([int(data[0][9:11]), int(data[0][11:13]), int(data[0][13:15])])
        if midnight == 1 and data_origin[0] < 12:
            data_origin[0] += 24
        time_data = time_to_sec(data_origin)

        if (time_data < time_sec):
                return True
        return False

def is_same_sensor(time, data):
        time_sec = time_to_sec(time)
        data_origin = [int(data[-1][9:11]), int(data[-1][11:13]), int(data[-1][13:15])]
        if midnight == 1 and data_origin[0] < 12:
            data_origin[0] += 24
        time_data = time_to_sec(data_origin)

        if (time_data < time_sec):
                return True
        return False
		
# load files
if mode == 0:
        with open(rgb_file_name, 'rb') as rgb_file:
                rgb_reader = csv.reader(rgb_file, delimiter=' ', quotechar='|')
                for row in rgb_reader:
                        rgb_data.append(row[0].replace('\'', '').split(','))
                rgb_data = rgb_data[1:]

with open(sensor_file_name, 'rb') as sensor_file:
        sensor_reader = csv.reader(sensor_file, delimiter=' ', quotechar='|')
        for row in sensor_reader:
                sensor_data.append(row[0].replace('\'', '').split(','))
        sensor_data = sensor_data[1:]

with open(origin_file_name, 'rb') as origin_file:
        origin_reader = csv.reader(origin_file, delimiter=' ', quotechar='|')
        for row in origin_reader:
                origin_data.append(row[0].replace('\'', '').split(','))
        origin_data = origin_data[1:]

time = [int(origin_data[0][-1][-13:-11]), int(origin_data[0][-1][-10:-8]), int(origin_data[0][-1][-7:-5])]

rgb_time = time[:]
rgb_time[2] += 3
if rgb_time[2] < 0:
	rgb_time[2] += 60
	rgb_time[1] -= 1
if rgb_time[1] < 0:
	rgb_time[1] += 60
	rgb_time[0] -= 1

rgb_idx = 0
sensor_idx = 0
origin_idx = 0

error_x = 0
error_y = 0
error_r = 0
error_g = 0
error_b = 0

result = []
if mode == 0:
        result_colname = ['SampleR', 'SampleG', 'SampleB', 'R', 'G', 'B', 'X', 'Y', 'Z', 'x', 'y', 'Lv', \
                                 'ErrorR', 'ErrorG', 'ErrorB', 'Errorx', 'Errory', 'ErrorLv']
        result.append(result_colname)
if mode == 1:
        result_colname = ['SampleR', 'SampleG', 'SampleB', 'X', 'Y', 'Z', 'x', 'y', 'Lv', 'Errorx', 'Errory', 'ErrorLv']
        result.append(result_colname)

raw_input('Press any key to generate data...')

if mode == 0:
	while is_same_rgb(rgb_time, rgb_data[rgb_idx]):
		rgb_idx += 1

while is_same_sensor(time, sensor_data[sensor_idx]):
	sensor_idx += 1
		
for i in range(color_num):
        add_time(time)
        add_time(rgb_time)

        result_rgb = []
        result_sensor = []
        if mode == 0:
                while is_same_rgb(rgb_time, rgb_data[rgb_idx]):
                        result_rgb.append(rgb_data[rgb_idx])
                        rgb_idx += 1
                if len(result_rgb) > 2:
                        result_rgb = result_rgb[1:-1]

        while is_same_sensor(time, sensor_data[sensor_idx]):
                #print str(sensor_idx) + ', ' + str(len(sensor_data))
                result_sensor.append(sensor_data[sensor_idx])
                sensor_idx += 1
        if len(result_sensor) > 2:
                result_sensor = result_sensor[1:-1]

        if debug == 1:
                for row in result_rgb+result_sensor:
                        print row

        result_row = []
        if mode == 0:
                result_r = 0
                result_g = 0
                result_b = 0    
                for row in result_rgb:
                        result_r += float(row[1])
                        result_g += float(row[2])
                        result_b += float(row[3])
                result_r /= len(result_rgb)
                result_g /= len(result_rgb)
                result_b /= len(result_rgb)

                result_row.append(result_r)
                result_row.append(result_g)
                result_row.append(result_b)

        result_sen = [0,0,0,0,0,0]
        for row in result_sensor:
                for i in range(1,7):
                        result_sen[i-1] += float(row[i])
        for i in range(6):
                result_sen[i] /= len(result_sensor)

        error_rgb = [0,0,0]
        if mode == 0:
                for i in range(1,4):
                        for j in range(1,len(result_rgb)-1):
                                error_rgb[i-1] += abs(float(result_rgb[j][i])-float(result_rgb[j-1][i]))
                if len(result_rgb) > 1:
                        error_rgb[i-1] /= (len(result_rgb) - 1)

        error_sensor = [0,0,0]
        for i in range(4,7):
                for j in range(1,len(result_sensor)-1):
                        error_sensor[i-4] += abs(float(result_sensor[j][i])-float(result_sensor[j-1][i]))
                if len(result_sensor) > 1:
                        error_sensor[i-4] /= (len(result_sensor) - 1)

        result_row = None
        if mode == 0:
                result_row = origin_data[origin_idx][0:3]+[result_r,result_g,result_b]+result_sen+error_rgb+error_sensor
        if mode == 1:
                result_row = origin_data[origin_idx][0:3]+result_sen+error_sensor
        result.append(result_row)

        origin_idx += 1
        #print str(origin_idx) + ', ' + str(len(origin_data)) + ',' + str(i)

        if debug == 1:
                raw_input('#####')

with open(output_file_name, 'wb') as fp:
    a = csv.writer(fp, delimiter=',')
    a.writerows(result)
