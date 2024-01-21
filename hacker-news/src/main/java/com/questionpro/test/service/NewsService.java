package com.questionpro.test.service;

import com.questionpro.test.entity.PeakComment;
import com.questionpro.test.entity.Story;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public interface NewsService {


    List<Story> getTopStories();

    Set<Story> getPastTopStories();
    SortedSet<PeakComment> getCommentsById(int storyId);
}
