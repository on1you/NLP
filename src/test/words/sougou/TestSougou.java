package test.words.sougou;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import main.words.sougou.SougouScelMdel;
import main.words.sougou.SougouScelReader;

public class TestSougou {
	@Test
	public void testReadScel()throws Exception{
		File file=new File("E:\\study\\数据资源\\词库\\北京市城市信息精选.scel"); 
		SougouScelMdel model = new SougouScelReader().read(file); 
		System.out.println(model.getName()); //名称 
		System.out.println(model.getType());  //类型 
		System.out.println(model.getDescription()); //描述 
		System.out.println(model.getSample());  //样例 
		Map<String,List<String>> words = model.getWordMap(); //词<拼音,词> 
		System.out.println(words.size()); 
		int loop = 1;
		for(String key : words.keySet()){
			if (loop > 50) {
				break;
			}
			System.out.println(key + " : " + words.get(key));
			loop++;
		}
	}
}
