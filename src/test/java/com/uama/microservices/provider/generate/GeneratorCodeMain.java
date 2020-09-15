package com.uama.microservices.provider.generate;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.util.ArrayList;
import java.util.List;

public class GeneratorCodeMain {
	public static void generator() {

		List<String> warnings = new ArrayList<String>();
		try {
			// 解析
			ConfigurationParser cp = new ConfigurationParser(warnings);
			Configuration config = cp.parseConfiguration(GeneratorCodeMain.class.getResourceAsStream("/mybatis-generator.xml"));
			// 是否覆盖
			DefaultShellCallback dsc = new DefaultShellCallback(true);
			MyBatisGenerator mg = new MyBatisGenerator(config, dsc, warnings);
			mg.generate(null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GeneratorCodeMain.generator();
		System.out.println("done!");
	}

}
