package nl.bd.garage.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    @JsonIgnore
    @OneToOne(mappedBy = "file", orphanRemoval = true)
    private Repair repair;

    @Lob
    private byte[] content;

    private String name;

    public File(byte[] content, String name) {
        this.content = content;
        this.name = name;
    }
}
