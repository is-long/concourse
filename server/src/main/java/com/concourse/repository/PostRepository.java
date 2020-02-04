package com.concourse.repository;

import com.concourse.models.posts.Post;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends CrudRepository<Post, String> {

    List<Post> findPostsByCourseId(String courseId);

    List<Post> findPostsByAuthorUserIdAndCourseId(String authorUserId, String courseId);

    default Post getPostElseNull(String postId){
        if (postId == null) return null;
        Optional<Post> optionalPost = findById(postId);
        return optionalPost.orElse(null);
    }

}
