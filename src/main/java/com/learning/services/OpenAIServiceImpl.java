package com.learning.services;

import com.learning.exceptions.AIException;
import com.learning.model.Answer;
import com.learning.model.InflationResponse;
import com.learning.model.Question;
import com.learning.tools.NinjaTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenAIServiceImpl  implements OpenAIService{

    private NinjaTools ninjaTools;

    private OpenAiApi openAiApi;

    @Autowired
    public void setNinjaTools(NinjaTools ninjaTools) {
        this.ninjaTools = ninjaTools;
    }

    @Autowired
    public void setOpenAiApi(OpenAiApi openAiApi) {
        this.openAiApi = openAiApi;
    }

    @Override
    public Answer getAnswer(Question question) {
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .build();
        ChatClient chatClient = ChatClient.create(openAiChatModel);

        String responseFormat = """
                Return a sentence describing the response data
                """;

        String response = chatClient.prompt()
                .user(u -> u.text(question.question() + "\n" + responseFormat))
                .tools(ninjaTools)
                .call()
                .content();

        if (response == null || response.isEmpty()) {
            throw new AIException("No response received from AI");
        }
        return new Answer(response);
    }
}
