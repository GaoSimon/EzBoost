#ifndef USER_H
#define USER_H

#include "utils_common.hpp"

class User{
public:
	unordered_map<Features, vector<int>> query_data;
	
	string test_data_path = "../../data/Credit/random_dataset/test_data.csv";

public:
	User()
	{
		ifstream filein(test_data_path);
		if(!filein)
		{
			cout << "No such files!" << endl;
		}
		string lines;
		getline(filein, lines);
		while(getline(filein, lines))
		{
			if(lines == "")
			{
				break;
			}
			vector<string> elements = SplitString(lines, ',');
			unordered_map<Features, int> row_data;
			for(int it = 0; it < test_features.size(); it++)
			{
				query_data[test_features[it]].push_back(atoi(elements[it].c_str()));                                                                  
			}
		}
		cout << "Read test data already!" << endl;
	}
	~User(){}
	pair<string, string> get_valueshare_json(int idx)
	{
		string res0(""),res1("");
		for(int i = 0; i < idx; i++)
		{
			for(int it = 0; it < test_features.size(); it++)
			{
				if(test_features[it] == LABEL)
					continue;
				auto value = query_data[test_features[it]][i];
				if(test_features[it] == ID)
				{
					res0 += to_string(value);
					res1 += to_string(value);
				}
				else
				{
					int value_a = int(rand());
					res0 += "," + to_string(value_a);
					res1 += "," + to_string(value-value_a);
				}
				
			}
			res0 += bline;
			res1 += bline;
		}
		return make_pair(res0,res1);
	}


};


#endif