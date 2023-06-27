#include "utils_server.hpp"
#include "utils_common.hpp"
#include "xg_boost.hpp"

string ip = ips[S2];

int main(int argc, char **argv) {
	Server S2_demo(argc, argv, 2);
	SVBoost booster;

	string filepath = model_path+"/S2_svboost_"+to_string(config.max_tree_num)+"_"+to_string(config.max_depth)+"_"+to_string(config.learning_rate)+".boost";
	if(filesystem::exists(filepath))
	{
		booster.Jsonload_svboost(filepath);
	}
	else
	{
		Server_Boost_training(ips[S2], S2_demo, booster);
		// string str_xgboost =  booster.Jsondump_svboost();
		// cout << str_xgboost << endl;
	}
	
	Server_predicting(ips[S2], S2_demo, booster);

	return 0;
}
