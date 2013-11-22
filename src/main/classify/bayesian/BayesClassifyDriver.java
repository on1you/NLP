package main.classify.bayesian;

import main.classify.common.Constants;
import main.classify.common.CreateTrainAndTestSample;

public class BayesClassifyDriver {

	public static void main(String[] args) throws Exception {
		//模型训练
		NativeBayesianModelTrain train = new NativeBayesianModelTrain();
		train.dataPreProcess(Constants.DATA_PRE_PROCESS_DIR);
		//创建训练及测试样本集
		CreateTrainAndTestSample ctt = new CreateTrainAndTestSample();
		ctt.process();
		//交叉验证分类测试
		NaiveBayesianClassifier nbClassifier = new NaiveBayesianClassifier();
		nbClassifier.NaiveBayesianClassifierMain(args);
	}
}
