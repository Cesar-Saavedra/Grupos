package cl.duoc.ms_grupos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Tabla "miembros_grupo" en la BD ms_grupos.
 *
 * Representa que un usuario pertenece a un grupo.
 * Un usuario puede estar en MUCHOS grupos.
 * Un grupo puede tener MUCHOS miembros.
 * --> Relacion muchos a muchos, resuelta con esta tabla intermedia.
 *
 * El campo "usuarioId" es el id del usuario en ms-login.
 * El campo "nombreUsuario" lo guardamos aqui para poder mostrarlo
 * en la lista de miembros sin tener que llamar a ms-usuarios.
 *
 * El campo "rolGrupo" indica si el miembro es ADMIN o MIEMBRO dentro
 * de este grupo especifico (no confundir con el Rol general de CardLink).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "miembros_grupo")
public class MiembroGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // El grupo al que pertenece este miembro
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    // Id del usuario en ms-login
    @Column(nullable = false)
    private Integer usuarioId;

    // Nombre del usuario guardado localmente para no llamar a ms-usuarios
    @Column(nullable = false)
    private String nombreUsuario;

    // Rol dentro de este grupo: ADMIN o MIEMBRO
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolGrupo rolGrupo;
}
