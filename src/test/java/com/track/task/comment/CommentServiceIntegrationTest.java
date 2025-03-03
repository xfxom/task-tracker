package com.track.task.comment;

import com.track.task.BaseIntegrationTest;
import com.track.task.dto.CommentDTO;
import com.track.task.exception.ForbiddenException;
import com.track.task.model.Comment;
import com.track.task.model.Task;
import com.track.task.model.User;
import com.track.task.model.Status;
import com.track.task.model.Priority;
import com.track.task.repository.CommentRepository;
import com.track.task.repository.TaskRepository;
import com.track.task.repository.UserRepository;
import com.track.task.repository.StatusRepository;
import com.track.task.repository.PriorityRepository;
import com.track.task.service.comment.CommentService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class CommentServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private PriorityRepository priorityRepository;

    private Status defaultStatus;
    private Priority defaultPriority;

    @BeforeEach
    public void setup() {
        defaultStatus = statusRepository.findById(1L).orElseThrow();
        defaultPriority = priorityRepository.findById(2L).orElseThrow();
    }

    private User createUser(String email, String username) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setPassword("password");
            return userRepository.save(user);
        });
    }

    private Task createTask(String title, String description, boolean visibility, User user) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setVisibility(visibility);
        task.setUser(user);
        task.setStatus(defaultStatus);
        task.setPriority(defaultPriority);
        return taskRepository.save(task);
    }

    @Test
    public void testAddCommentToVisibleTask() {
        User user = createUser("test@example.com", "testuser");
        Task task = createTask("Test Task", "Test Task Description", true, user);

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("This is a test comment");

        Comment comment = commentService.addComment(task.getId(), commentDTO, user.getEmail());

        assertThat(comment).isNotNull();
        assertThat(comment.getContent()).isEqualTo("This is a test comment");
        assertThat(comment.getTask().getId()).isEqualTo(task.getId());
        assertThat(comment.getUser().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void testAddCommentToHiddenTaskThrowsForbidden() {
        User user = createUser("test@example.com", "testuser");
        Task task = createTask("Hidden Task", "This task is hidden", false, user);

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("Trying to comment on hidden task");

        assertThatThrownBy(() -> commentService.addComment(task.getId(), commentDTO, user.getEmail()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    public void testReplyOnComment() {
        User user = createUser("replyuser@example.com", "replyuser");
        Task task = createTask("Task for reply", "Task description", true, user);

        CommentDTO parentDto = new CommentDTO();
        parentDto.setContent("Parent comment");
        Comment parentComment = commentService.addComment(task.getId(), parentDto, user.getEmail());

        CommentDTO replyDto = new CommentDTO();
        replyDto.setContent("This is a reply");

        Comment replyComment = commentService.replyOnComment(task.getId(), parentComment.getId(), replyDto, user.getEmail());

        assertThat(replyComment).isNotNull();
        assertThat(replyComment.getParent()).isNotNull();
        assertThat(replyComment.getParent().getId()).isEqualTo(parentComment.getId());
    }

    @Test
    public void testGetAllComments() {
        User user = createUser("allcomments@example.com", "allcomments");
        Task task = createTask("Task for all comments", "Task description", true, user);

        CommentDTO commentDto1 = new CommentDTO();
        commentDto1.setContent("First comment");
        commentService.addComment(task.getId(), commentDto1, user.getEmail());

        CommentDTO commentDto2 = new CommentDTO();
        commentDto2.setContent("Second comment");
        commentService.addComment(task.getId(), commentDto2, user.getEmail());

        List<Comment> comments = commentService.getAll();
        assertThat(comments).extracting("content").contains("First comment", "Second comment");
    }

    @Test
    public void testGetCommentById() {
        User user = createUser("getbyid@example.com", "getbyid");
        Task task = createTask("Task for getById", "Task description", true, user);

        CommentDTO commentDto = new CommentDTO();
        commentDto.setContent("Comment to retrieve");
        Comment comment = commentService.addComment(task.getId(), commentDto, user.getEmail());

        assertThat(commentService.getById(comment.getId())).isPresent()
                .get()
                .extracting(Comment::getContent)
                .isEqualTo("Comment to retrieve");
    }

    @Test
    public void testGetAllByEmail() {
        User user = createUser("byemail@example.com", "byemail");
        Task task = createTask("Task for email comments", "Task description", true, user);

        CommentDTO commentDto1 = new CommentDTO();
        commentDto1.setContent("Email comment one");
        commentService.addComment(task.getId(), commentDto1, user.getEmail());

        CommentDTO commentDto2 = new CommentDTO();
        commentDto2.setContent("Email comment two");
        commentService.addComment(task.getId(), commentDto2, user.getEmail());

        List<Comment> comments = commentService.getAllByEmail(user.getEmail());
        assertThat(comments).hasSize(2);
        assertThat(comments).extracting("content").contains("Email comment one", "Email comment two");
    }

    @Test
    public void testGetAllByTaskIdForNonAdmin() {

        User author = createUser("author@example.com", "author");
        User other = createUser("other@example.com", "other");

        Task task = createTask("Task for non-admin test", "Task description", true, author);

        CommentDTO commentDto1 = new CommentDTO();
        commentDto1.setContent("Comment by author");
        commentService.addComment(task.getId(), commentDto1, author.getEmail());

        CommentDTO commentDto2 = new CommentDTO();
        commentDto2.setContent("Comment by other");
        commentService.addComment(task.getId(), commentDto2, other.getEmail());

        List<Comment> commentsForOther = commentService.getAllByTaskId(task.getId(), other.getEmail());

        assertThat(commentsForOther).hasSize(1);
        assertThat(commentsForOther.get(0).getUser().getEmail()).isEqualTo(other.getEmail());
    }

    @Test
    public void testGetCommentsForAdmin() {
        User admin = createUser("admin@example.com", "admin");

        Task task = createTask("Task for admin comments", "Task description", true, admin);

        CommentDTO commentDto = new CommentDTO();
        commentDto.setContent("Admin comment");
        commentService.addComment(task.getId(), commentDto, admin.getEmail());

        List<Comment> adminComments = commentService.getComments(admin.getEmail());
        List<Comment> allComments = commentService.getAll();

        assertThat(adminComments).hasSameSizeAs(allComments);
    }
}
