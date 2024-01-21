package com.questionpro.test.controller;

import com.questionpro.test.constants.AppConstants;
import com.questionpro.test.entity.PeakComment;
import com.questionpro.test.entity.Story;
import com.questionpro.test.exception.NoStoryFoundException;
import com.questionpro.test.service.NewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/hacker-news")
@Slf4j
public class HackerNewsController {

    @Autowired
    private NewsService newsService;

    /*
     *@Author: Yash.joshi
     *@Description: API to fetch the last 10 stories based on their score.
     * The story is visible for 15 minutes and gets updated at each 15-minute time span.
     *@return : List<Story>
     */

    @GetMapping("/top-stories")
    public List<Story> listTopStories() {
        log.info("/top-stories -> service");
        List<Story> topStories = newsService.getTopStories();
        if (topStories.isEmpty()) {
            log.error("/top-stories -> service -> Error while fetching records");
            throw new NoStoryFoundException(AppConstants.NO_STORY_FOUND);
        }
        log.info("/top-stories -> service -> success");
        return topStories;
    }

    /*
     *@Author: Yash.joshi
     *@Description: API to return all the stories that were served previously from the 1st endpoint (/top-stories).
     *@return : Set<Story>
     */
    @GetMapping("/past-stories")
    public Set<Story> listOfPastStories() {
        log.info("/past-stories -> service");
        Set<Story> pastStories = newsService.getPastTopStories();
        if (pastStories.isEmpty()) {
            log.error("/past-stories -> service -> Error while fetching records");
            throw new NoStoryFoundException(AppConstants.NO_STORY_FOUND);
        }
        log.info("/past-stories -> service -> success");
        return pastStories;
    }

    /*
     *@Author: Yash.joshi
     *@Description: API to return 10 comments (max) on a given story sorted by a total number of child comments. Each comment should contain comment text, the user’s hacker news handle.
     * @return: list of comments text username and profile age.
     */
    @GetMapping("/comments/{storyId}")
    public List<PeakComment> listOfComments(@PathVariable int storyId) {
        log.info("/comments -> service");

        //returning the top 10 comments into a list
        List<PeakComment> peakComments = newsService.getCommentsById(storyId).stream().limit(10)
                .collect(Collectors.toList());

        if (peakComments.isEmpty()) {
            throw new NoStoryFoundException(AppConstants.NO_STORY_FOUND);
        }

        return peakComments;
    }


}
