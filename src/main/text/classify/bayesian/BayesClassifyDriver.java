package main.text.classify.bayesian;

import main.text.classify.common.Constants;
import main.text.classify.common.CreateTrainAndTestSample;
import main.text.classify.common.DataPretreatment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BayesClassifyDriver {
	 
	private static final Logger log = LoggerFactory.getLogger(BayesClassifyDriver.class);
	
	public static void main(String[] args) throws Exception {
		int loop = 1;
		DataPretreatment prev = new DataPretreatment();
		NativeBayesianModelTrain train = new NativeBayesianModelTrain();
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
