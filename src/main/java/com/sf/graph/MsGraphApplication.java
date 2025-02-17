package com.sf.graph;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ai.autoconfigure.azure.openai.AzureOpenAiAutoConfiguration;

@SpringBootApplication(exclude = { AzureOpenAiAutoConfiguration.class })
public class MsGraphApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsGraphApplication.class, args);
	}

}
