package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name="pages")
public class Page {
    @Id
    @GeneratedValue (strategy=GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigserial")
    private Long id;

    @ManyToOne
    @JoinColumn(name="site_id")
    private Site site;

    @Column(name = "path", columnDefinition = "text", length = 1000)
    private String path;

    @Column(name = "code", columnDefinition = "integer")
    private Integer code;

    @Column(name = "content", columnDefinition = "text")
    private String content;
}
