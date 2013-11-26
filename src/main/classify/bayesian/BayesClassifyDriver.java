package main.classify.bayesian;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.classify.common.Constants;
import main.classify.common.CreateTrainAndTestSample;


public class BayesClassifyDriver {
	
	private static final Logger log = LoggerFactory.getLogger(BayesClassifyDriver.class);
	
	public static void main(String[] args) throws Exception {
		int loop = 1;
		while(true){
			log.info("The {}th round of training", loop);
			//模型训练
			NativeBayesianModelTrain train = new NativeBayesianModelTrain();
			if (loop == 1) {
				train.dataPreProcess(Constants.DATA_PRE_PROCESS_DIR);
			}
			train.saveModel();
			//创建训练及测试样本集
			CreateTrainAndTestSample ctt = new CreateTrainAndTestSample();
			ctt.process();
			//交叉验证分类测试
			NaiveBayesianClassifier nbClassifier = new NaiveBayesianClassifier();
			nbClassifier.NaiveBayesianClassifierMain(args);
			loop ++;
		}
	}
}
