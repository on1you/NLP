package main.classify.bayesian;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.classify.common.Constants;
import main.classify.common.CreateTrainAndTestSample;
import main.classify.common.DataPretreatment;


public class BayesClassifyDriver {
	 
	private static final Logger log = LoggerFactory.getLogger(BayesClassifyDriver.class);
	
	public static void main(String[] args) throws Exception {
		int loop = 1;
		NativeBayesianModelTrain train = new NativeBayesianModelTrain();
		DataPretreatment prev = new DataPretreatment();
		CreateTrainAndTestSample ctt = new CreateTrainAndTestSample();
		NaiveBayesianClassifier nbClassifier = new NaiveBayesianClassifier();
		while(true){
			log.info("The {}th round of training", loop);
			//模型训练
			if (loop == 1) {
				prev.process(Constants.DATA_PRE_PROCESS_DIR);
			}
			train.saveModel();
			//创建训练及测试样本集
			ctt.process();
			//交叉验证分类测试
			nbClassifier.NaiveBayesianClassifierMain(args);
			loop ++;
		}
	}
}
