package com.concourse.models.posts;

import com.concourse.models.users.User;
import com.concourse.tools.StringTools;
import lombok.Data;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Post {

    @Id
    private String id = StringTools.generateID(32);
    private Long postDate = new Date().getTime();


    private Integer likeCount = 0;

    @ElementCollection
    private Map<String, Integer> likesUserIDMap = new HashMap<>();


    private String courseId;

    @Lob
    private String content;

    private String authorUserId;
    private String authorName;
    private String authorType;

    public void setAuthor(User user) {
        this.authorUserId = user.getEmail();
        this.authorName = user.getName();
        this.authorType = user.getRole();
    }

    public Post() {
    }

    public Post(String courseId, String content, User author) {
        this.courseId = courseId;
        this.content = content;
        setAuthor(author);
    }

    public void like(String userId, int value) {
        Integer vote = likesUserIDMap.get(userId);

        //if user has never liked/disliked the post before
        if (vote == null) {
            likesUserIDMap.put(userId, value);
            likeCount += value;
        } else {
            likeCount -= vote;  //undo old like

            //undo like/dislike
            if (value == 0){
                likesUserIDMap.remove(userId);
            } else {
               likesUserIDMap.put(userId, value);
               likeCount += value;
            }
        }
    }
}
