package com.questionpro.test.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.questionpro.test.config.ServerUtils;
import com.questionpro.test.constants.AppConstants;
import com.questionpro.test.entity.Comment;
import com.questionpro.test.entity.PeakComment;
import com.questionpro.test.entity.Story;
import com.questionpro.test.exception.InternalServerException;
import com.questionpro.test.exception.NoStoryFoundException;
import com.questionpro.test.service.CacheManagerService;
import com.questionpro.test.service.NewsService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NewsServiceImpl implements NewsService {
    // Set to store all past stories
    private static final Set<Story> pastStorySet = new HashSet<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    @Autowired
    private CacheManagerService cacheService;

    /*
     * @Author: Yash.joshi
     */
    @Override
    public List<Story> getTopStories() {
        List<Story> topStoriesList;
        // checking existing records in cache
        if (cacheService.get(AppConstants.CACHE_TOP_STORIES) == null) {
            topStoriesList = ListOfTopStories();
            try {
                cacheService.set(AppConstants.CACHE_TOP_STORIES, ServerUtils.getObjectMapper().writeValueAsString(topStoriesList));
            } catch (JsonProcessingException e) {
                log.error(e.toString());
                throw new InternalServerException("Error occurred " + e.getMessage());
            }
        } else {
            try {
                topStoriesList = ServerUtils.getObjectMapper().readValue(cacheService.get(AppConstants.CACHE_TOP_STORIES).toString(), new TypeReference<List<Story>>() {
                });
            } catch (Exception e) {
                log.error(e.toString());
                throw new InternalServerException("Error " + e.getMessage());
            }
        }
        return topStoriesList;
    }

    // returning list of top stories based on score, used executor framework to enhance the performance.
    private List<Story> ListOfTopStories() {
        List<Integer> topStoriesIds = ServerUtils.getTopStoryIds();

        // Use CompletableFuture to fetch story details concurrently
        List<CompletableFuture<Story>> futures = topStoriesIds.stream().map(id -> CompletableFuture.supplyAsync(() -> ServerUtils.getStory(id), executorService)).collect(Collectors.toList());

        // Wait for all futures to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // Combine the results
        CompletableFuture<List<Story>> combinedFuture = allOf.thenApply(v -> futures.stream().map(CompletableFuture::join).sorted(Comparator.comparing(Story::getScore).reversed()).limit(10).collect(Collectors.toList()));

        // Block and get the final result
        List<Story> topStories = combinedFuture.join();

        addToPastStorySet(topStories);

        return topStories;


    }

    //  the purpose of this method is to keep a set of past stories and update their scores if they appear again in the current set of top stories. This mechanism could be useful for tracking changes in the scores of specific stories over time or across different requests.
    private void addToPastStorySet(List<Story> topTenStories) {
       /* iterates through each topStory in the topTenStories list.
         For each topStory, it checks if the pastStorySet already contains a story equal to the current topStory.
        If yes, it finds the matching story in pastStorySet and updates its score with the score of the current topStory.
         If no, it adds the current topStory to the pastStorySet.
         */
        topTenStories.stream().forEach(topStory -> {
            if (pastStorySet.contains(topStory)) {
                pastStorySet.stream().filter(pastStory -> pastStory.equals(topStory)).findFirst().ifPresent(story -> story.setScore(topStory.getScore()));
            } else {
                pastStorySet.add(topStory);
            }
        });
    }

    // previously served top stories
    public Set<Story> getPastTopStories() {
        if (pastStorySet != null) {
            return pastStorySet;
        }
        throw new NoStoryFoundException("No past story found. please try /top-stories first to get past stories");
    }


    @Override
    public SortedSet<PeakComment> getCommentsById(int storyId) {
        log.info("retrieving top 10 comments for Story with id: {}", storyId);
        //The Comment instances in the commentSet will be sorted in descending order of their totalComments, and for instances with the same count, they will be sorted in ascending order based on the same count.
        SortedSet<PeakComment> peakCommentSet = new TreeSet<>(Comparator.comparing(PeakComment::getTotalComments).reversed().thenComparing(PeakComment::getTotalComments));

        Story story = ServerUtils.getStory(storyId);
        if (story != null) {
            //storing child comments
            int[] childComments = story.getKids();
            // fetch all child comments against each comment in the story
            if (childComments != null) {
                for (int commentId : childComments) {
                    Comment comment = ServerUtils.getComment(commentId);
                    if (comment != null) {
                        int commentCount = 1 + ServerUtils.getCountOfComments(comment);

                        PeakComment peakComment = new PeakComment(comment.getText(), comment.getBy(), ServerUtils.calculateHowOldTheUserIs(comment.getBy()), commentCount);

                        peakCommentSet.add(peakComment);
                    }
                }
            } else {
                throw new  NoStoryFoundException(AppConstants.NO_COMMENT_FOUND_FOR_THE_STORY);
            }
        } else {
            throw new  NoStoryFoundException(AppConstants.NO_STORY_FOUND);
        }
        return peakCommentSet;

    }


    @PreDestroy
    public void shutdownExecutorService() {
        executorService.shutdown();
    }
}
