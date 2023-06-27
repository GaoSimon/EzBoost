#ifndef COMMON_H
#define COMMON_H

#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <cmath>
#include <numeric>
#include <algorithm>
#include <functional>
#include <mutex>
#include <set>
#include <chrono>
#include <omp.h>
#include <random>
#include <unordered_set>
#include <map>
#include <unordered_map>
#include <filesystem> 
#include <iomanip>
#include <eigen3/Eigen/Dense>
#include "zmq.hpp"
#include "zmq_addon.hpp"

using namespace Eigen;
using namespace std;
#define client_num 4
class Config {
public:
	const int init_threshold_num = 32;
	const int samples_num = 24000;
	const int max_tree_num = 6;
	const int max_depth = 3;
	const double learning_rate = 0.3;
	const int query_num = 1000;
};
const Config config;

string model_path = "../../BoostModel_2";
string sep("-");
string bline("\n");


enum Entity {S1,S2,P1,P2,P3,P4,U,Noen=-1};
Entity client[client_num] = {P1,P2,P3,P4};
Entity acp = P1;
enum Features {ID,LIMIT_BAL,SEX,EDUCATION,MARRIAGE,AGE,
	PAY_1,PAY_2,PAY_3,PAY_4,PAY_5,PAY_6,
	BILL_AMT1,BILL_AMT2,BILL_AMT3,BILL_AMT4,BILL_AMT5,BILL_AMT6,
	PAY_AMT1,PAY_AMT2,PAY_AMT3,PAY_AMT4,PAY_AMT5,PAY_AMT6,LABEL,Nofea=-1};
vector<Features> test_features = 
{ID,LIMIT_BAL,SEX,EDUCATION,MARRIAGE,AGE,
PAY_1,PAY_2,PAY_3,PAY_4,PAY_5,PAY_6,
BILL_AMT1,BILL_AMT2,BILL_AMT3,BILL_AMT4,BILL_AMT5,BILL_AMT6,
PAY_AMT1,PAY_AMT2,PAY_AMT3,PAY_AMT4,PAY_AMT5,PAY_AMT6,LABEL};
unordered_map<Entity, vector<Features>> individual_features = {
	{P1, {ID,PAY_AMT1,PAY_AMT2,PAY_AMT3,PAY_AMT4,PAY_AMT5,PAY_AMT6,LABEL}},
	{P2, {ID,LIMIT_BAL,SEX,EDUCATION,MARRIAGE,AGE}},
	{P3, {ID,PAY_1,PAY_2,PAY_3,PAY_4,PAY_5,PAY_6}},
	{P4, {ID,BILL_AMT1,BILL_AMT2,BILL_AMT3,BILL_AMT4,BILL_AMT5,BILL_AMT6}}
};
unordered_map<Entity, string> individual_datafilepath = {
	{P1, "../../data/Credit/use_dataset/APdata_train.csv"},
	{P2, "../../data/Credit/use_dataset/PPdata_train_0.csv"},
	{P3, "../../data/Credit/use_dataset/PPdata_train_1.csv"},
	{P4, "../../data/Credit/use_dataset/PPdata_train_2.csv"}
};
unordered_map<Entity, string> paths = {
	{P1, "../../data/Credit/random_dataset/datap1.csv"},
	{P2, "../../data/Credit/random_dataset/datap2.csv"},
	{P3, "../../data/Credit/random_dataset/datap3.csv"},
	{P4, "../../data/Credit/random_dataset/datap4.csv"}
};
unordered_map<Entity, string> attributes = {
	{P1, "ID,PAY_AMT1,PAY_AMT2,PAY_AMT3,PAY_AMT4,PAY_AMT5,PAY_AMT6,label"},
	{P2, "ID,LIMIT_BAL,SEX,EDUCATION,MARRIAGE,AGE"},
	{P3, "ID,PAY_1,PAY_2,PAY_3,PAY_4,PAY_5,PAY_6"},
	{P4, "ID,BILL_AMT1,BILL_AMT2,BILL_AMT3,BILL_AMT4,BILL_AMT5,BILL_AMT6"}
};
enum TranTypes {BMtype, GLtype, TStype, SOtype, NewTree, ShutDown, Query, Value, Comparison, NodeID, Prediction, Start};

vector<string> ips = {"tcp://127.0.0.1:4001", "tcp://127.0.0.1:4002", "tcp://127.0.0.1:5001", 
					  "tcp://127.0.0.1:5002", "tcp://127.0.0.1:5003", "tcp://127.0.0.1:5004",
					  "tcp://127.0.0.1:6001"};

typedef Array<bool, 1, Dynamic> Myvec;
typedef Array<bool, Dynamic, Dynamic> Mymat;
struct GradAndHess {
	uint64_t grad;
	uint64_t hess;
};
typedef vector<GradAndHess> GradAndHess_list; 

int bw = 64;
uint64_t mask = (bw == 64 ? -1 : ((1ULL << bw) - 1));


vector<string> SplitString(string str, const char split)
{
	vector<string> res;
	istringstream iss(str);	
	string token;			
	while (getline(iss, token, split))	// 以split为分隔符
	{
		res.push_back(token);
	}
	return res;
}

double round_double(double val, int precision) {
    double factor = pow(10, precision);
    return round(val * factor) / factor;
}

string JsonDump_Gradients_list(GradAndHess_list gradients_list)
{
	string json("");
	for(auto gradient : gradients_list){
		json += to_string(gradient.grad) + sep + to_string(gradient.hess) +bline;
	}
	// cout << "JsonDump_Gradients_list already!" << endl;
	return json;
}

GradAndHess_list JsonLoad_Gradients_list(string json)
{
	GradAndHess_list gradients_list;
	vector<string> str_gradients_list = SplitString(json, '\n');
	for(string str_gradient : str_gradients_list)
	{
		vector<string> str_sub_gradient = SplitString(str_gradient, '-');
		GradAndHess gradients = {strtoull(str_sub_gradient[0].c_str(), NULL, 10), strtoull(str_sub_gradient[1].c_str(), NULL, 10)};
		gradients_list.push_back(gradients);
	}
	// cout << "JsonLoad_Gradients_list already!" << endl;
	return gradients_list;
}
Myvec JsonLoad_samples_onehot(string str_onehot)
{
	assert(str_onehot.length() == config.samples_num);
	Myvec onehot(1, config.samples_num);
	for(int i = 0; i < str_onehot.length(); i++)
	{
		onehot(0,i) = str_onehot[i] == '1' ? true : false;
	}
	return onehot;
}
string JsonDump_samples_onehot(Myvec onehot)
{
	string str("");
	for(int i = 0; i < onehot.cols(); i++)
	{
		str += to_string(onehot(0,i));
	}
	return str;
}

pair<string, string> JsonDump_Share_Thresholds(vector<int> thresholds)
{
	string json_a, json_b;
	for(auto ts : thresholds)
	{
		int ts_a = int(rand());
		int ts_b = ts - ts_a;
		json_a += to_string(ts_a)+bline;
		json_b += to_string(ts_b)+bline;
	}
	return make_pair(json_a,json_b);
}

vector<int> JsonLoad_Thresholds_share(string str_thresholds)
{
	vector<int> thresholds;
	vector<string> str_threshold = SplitString(str_thresholds, '\n');
	for(auto str_ts : str_threshold)
	{
		thresholds.push_back(atoi(str_ts.c_str()));
	}
	return thresholds;
}

pair<string,string> JsonDump_Bucket_Matrix(pair<Mymat,Mymat> BM_share)
{
	string json1(""),json2("");
	// cout << BM_share.first << endl;
	bool* data1 = new bool[BM_share.first.rows()*BM_share.first.cols()];
	Map<Array<bool, Dynamic, Dynamic>>(data1, BM_share.first.rows(), BM_share.first.cols()) = BM_share.first;

	bool* data2 = new bool[BM_share.second.rows()*BM_share.second.cols()];
	Map<Array<bool, Dynamic, Dynamic>>(data2, BM_share.second.rows(), BM_share.second.cols()) = BM_share.second;
	int row = BM_share.first.rows(), col = BM_share.first.cols();
    for (int i = 0; i < row; i++) {
		for(int j = 0; j < col; j++)
		{
			json1 += to_string(data1[i+j*row]);
			json2 += to_string(data2[i+j*row]);
		}
		json1 += "\n";
		json2 += "\n";
    }
	// cout << json1 << endl;
	// cin.get();
	return make_pair(json1,json2);
}

Mymat JsonLoad_Bucket_Matrix(string input)
{
	vector<string> str_bkts = SplitString(input, '\n');
	Mymat bktmat(str_bkts.size(), config.samples_num);
	bktmat.setConstant(false);
	for(int i = 0; i < str_bkts.size(); i++)
	{
		auto str_bkt = str_bkts[i];
		for(int j = 0; j < config.samples_num; j++)
		{
			if(str_bkt[j] == '1')
			{
				bktmat(i,j) = true;
			}
		}
	}
	// cout << "JsonLoad_Bucket_Matrix already!" << endl;
	return bktmat;
}


void zmq_send_msg( string str, Entity target)
{
	zmq::context_t context (1);
	zmq::socket_t socket (context, ZMQ_DEALER);
	string ip = ips[target];
	socket.connect (ip);
	zmq::message_t msg(str.size());
	memcpy(msg.data(), str.data(), str.size());
	if(!socket.connected())  //判断连接是否建立成功
	{
		 cout << "Not connected." <<  endl;
	}
	bool ok = socket.send(msg);
	
	//  等待服务器返回的响应
	zmq::message_t reply;
	socket.recv(&reply);
	char* buffer = new char[reply.size()+1];
	memcpy(buffer, reply.data(), reply.size());
	buffer[reply.size()] = '\0';
	// cout << string(buffer) << endl;
	if(string(buffer) == "close")
	{
		socket.close();
		context.close();
		return;
	}
	else
	{
		cout << "error" << endl;
		return;
	}
}

int get_int_until_chr(string& str, const char chr)
{
	size_t pos = str.find(chr);
	int res = atoi(str.substr(0,pos).c_str());
	str = str.substr(pos+1);
	return res;
}


string getCurrentTimeAsString() {
    auto now =  chrono::system_clock::now();
	time_t t =  chrono::system_clock::to_time_t(now);
	tm tm = * localtime(&t);
	ostringstream oss;
    oss <<  put_time(&tm, "%Y-%m-%d %H-%M");
    return oss.str();
}
class Timer{
public:
	chrono::time_point<chrono::high_resolution_clock, chrono::nanoseconds> last_time;
	int waitting_time = 0;
	void record()
	{
		last_time = chrono::high_resolution_clock::now();
	}
	void show()
	{
		auto duration = chrono::high_resolution_clock::now()-last_time;
		cout << "时间开销: " << duration.count()/ 1000000.0 << " ms." << endl;
	}
	chrono::time_point<chrono::high_resolution_clock, chrono::nanoseconds> now()
	{
		return chrono::high_resolution_clock::now();
	}
	void compute(chrono::time_point<chrono::high_resolution_clock, chrono::nanoseconds> start, chrono::time_point<chrono::high_resolution_clock, chrono::nanoseconds> end, int waitting_time = 0)
	{	
		auto duration = end-start;
		cout << "时间开销: " << (duration.count()/ 1000000.0 - waitting_time*1000) << " ms." << endl;
	}
	void wait()
	{
		sleep(1);
		waitting_time++;
	}
};
Timer timer;

class Comer{
public:
	uint64_t size = 0;
	void add(uint64_t input)
	{
		size += input;
	}
	void show(string str)
	{
		cout << str << "通信开销为:" << fixed << setprecision(0) << (double)size << "B" << endl; 
		size = 0;
	}
};
Comer comer, comer2;
#endif