package cl.duoc.ms_grupos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Tabla "grupos" en la BD ms_grupos.
 *
 * Un grupo es una comunidad creada por un usuario de CardLink.
 * Por ejemplo: "Jugadores de Magic Santiago Centro", "Coleccionistas Pokemon".
 *
 * El campo "creadoPorUsuarioId" guarda el id del usuario que creo el grupo.
 * Ese id viene del JWT (es el mismo id de la tabla usuarios en ms-login).
 * No usamos una FK entre BDs distintas, solo guardamos el numero.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "grupos")
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    // Id del usuario que creo el grupo (viene del JWT de ms-login)
    @Column(nullable = false)
    private Integer creadoPorUsuarioId;

    // Nombre del creador guardado aqui para mostrarlo facilmente
    // sin necesidad de llamar a ms-usuarios
    @Column(nullable = false)
    private String creadoPorNombre;
}
