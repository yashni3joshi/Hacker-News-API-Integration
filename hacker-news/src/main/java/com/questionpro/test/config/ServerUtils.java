package com.questionpro.test.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.questionpro.test.constants.AppConstants;
import com.questionpro.test.entity.Comment;
import com.questionpro.test.entity.Story;
import com.questionpro.test.entity.User;
import com.questionpro.test.exception.InternalServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ServerUtils {
    private static final String HACKER_NEWS_API_URL = AppConstants.HACKER_NEWS_API_BASE_URL + AppConstants.TOP_STORIES_END_POINT;
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static List<Integer> getTopStoryIds() {
        try {
            Integer[] topStories = restTemplate.getForObject(HACKER_NEWS_API_URL, Integer[].class);
            return Arrays.asList(topStories);
        } catch (Exception e) {
            log.error("Error fetching top story IDs", e);
            throw new InternalServerException("Error occurred while fetching the stories.");
        }
    }

    public static Story getStory(int storyId) {
        try {
            String apiUrl = AppConstants.HACKER_NEWS_API_BASE_URL + AppConstants.ITEM_END_POINT + storyId + AppConstants.EXTENSION;
            return restTemplate.getForObject(apiUrl, Story.class);
        } catch (Exception e) {
            log.error("Error fetching story with ID " + storyId, e);
            throw new InternalServerException("Error");
        }
    }

    public static Comment getComment(int commentId) {
        try {
            String apiUrl = AppConstants.HACKER_NEWS_API_BASE_URL + AppConstants.ITEM_END_POINT + commentId + AppConstants.EXTENSION;
            return restTemplate.getForObject(apiUrl, Comment.class);

        } catch (Exception e) {
            log.error(e.toString());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public static int getCountOfComments(Comment comment) {
        if (comment == null) {
            return 0;
        }
        int count = 0;
        /*iterates through each child comment ID in comment.getKids().
        For each child comment ID, it fetches the corresponding child comment using Hacker News API.
        It recursively calls itself (getCountOfComments) for the fetched child comment to get its total count of comments.
        The count is added to the total count. */
        if (comment.getKids() != null) {

            count = count + comment.getKids().length;

            for (int commentId : comment.getKids()) {
                try {
                    Comment childComment = objectMapper.readValue(new URL(AppConstants.HACKER_NEWS_API_BASE_URL + AppConstants.ITEM_END_POINT + commentId + AppConstants.EXTENSION), Comment.class);
                    count += getCountOfComments(childComment);
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage());
                    throw new InternalServerException(" Error occurred at fetching child comments");
                }

            }

        }

        return count;


    }


    public static int calculateHowOldTheUserIs(String name) {


        User user = null;

        try {
            user = objectMapper.readValue(new URL(AppConstants.HACKER_NEWS_API_BASE_URL + AppConstants.USER_END_POINT + name + AppConstants.EXTENSION), User.class);
        } catch (Exception e) {
            log.error("Error getting age for user: {}", name);
        }

        if (user != null) {
            return Period.between(LocalDate.ofInstant(Instant.ofEpochMilli(user.getCreated() * 1000L), ZoneId.systemDefault()), LocalDate.now()).getYears();
        }

        return 0;
    }
}
