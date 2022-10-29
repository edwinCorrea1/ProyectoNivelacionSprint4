/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo.Controllers;

import com.example.demo.Controllers.exceptions.NonexistentEntityException;
import com.example.demo.Models.Usuarios;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
        
@CrossOrigin(origins = "*", methods = {RequestMethod.POST,RequestMethod.GET, RequestMethod.DELETE, RequestMethod.PUT})
@RequestMapping("/usuarios")
@RestController
public class UsuariosJpaController implements Serializable {

    public UsuariosJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    //@CrossOrigin(origins="*")
    @PostMapping()
    public String create(@RequestBody Usuarios usuarios) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(usuarios);
            em.getTransaction().commit();
            return "ok";
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    @CrossOrigin(origins="*")
    @PutMapping()
    public String edit(@RequestBody Usuarios usuarios) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            usuarios = em.merge(usuarios);
            em.getTransaction().commit();
            return "ok";
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = usuarios.getId();
                if (findUsuarios(id) == null) {
                    throw new NonexistentEntityException("The usuarios with id " + id + " no longer exists.");
                }
            }
            
            throw ex;
            
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    @CrossOrigin(origins="*")
    @DeleteMapping("/{id}")
    public String destroy(@PathVariable("id") Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuarios usuarios;
            
            try {
                usuarios = em.getReference(Usuarios.class, id);
                usuarios.getId();
                
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The usuarios with id " + id + " no longer exists.", enfe);
            }
            em.remove(usuarios);
            em.getTransaction().commit();
            return "ok";
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    @CrossOrigin(origins="*")
    @GetMapping("/login")
    public Map<String,String> login(@RequestParam("correo") String correo, @RequestParam("contrasena") String contrasena){
        System.out.println("" + correo + "," + contrasena);
        EntityManager em = getEntityManager();
        HashMap<String,String> rta = new HashMap<>();
       //String rta = "";
        try{
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Usuarios.class));
            String query = "SELECT * FROM usuarios WHERE correo = '" +  correo + "' AND contrasena = '" + contrasena + "'";
            System.out.println(""  + query);
            Query q = em.createNativeQuery(query);
            
            List<Usuarios> lu = q.getResultList();
            System.out.println("cant." + lu.size());
            if(lu.size() == 0){
                rta.put ("msj","Datos de Acceso Incorrectos");
            }else{
                
                List<Object> u = q.getResultList();
                Object[] le = (Object []) u.get(0);
                System.out.println("nombre: " + le[1]);
                System.out.println("Tipo: " + le[4]);
                rta.put("msj", "ok");
                rta.put("tipo",""+ le[4]);
            }
        }catch(Exception ex){
            System.out.println("" + ex);
        }
        return rta;
    }
    
    @CrossOrigin(origins="*")
    @GetMapping()
    public List<Usuarios> findUsuariosEntities() {
        return findUsuariosEntities(true, -1, -1);
    }

    public List<Usuarios> findUsuariosEntities(int maxResults, int firstResult) {
        return findUsuariosEntities(false, maxResults, firstResult);
    }

    private List<Usuarios> findUsuariosEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Usuarios.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }
    
    @CrossOrigin(origins="*")
    @GetMapping("/id")
    public Usuarios findUsuarios(@RequestParam("id") Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuarios.class, id);
        } finally {
            em.close();
        }
    }

    public int getUsuariosCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Usuarios> rt = cq.from(Usuarios.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
