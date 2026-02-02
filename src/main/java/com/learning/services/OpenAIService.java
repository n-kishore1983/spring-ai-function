package com.learning.services;

import com.learning.model.Answer;
import com.learning.model.Question;

public interface OpenAIService {

    Answer getAnswer(Question question);
}
