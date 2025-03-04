package utils;

import main.AdminController;
import model.Employee;

/*----------------Stockage de l'utilisateur en mémoire et vérification mode Admin---------------*/


public class AuthService {

    /*------------SINGLETON--------------*/
    private static AuthService instance;

    public static AuthService getInstance(){
        if(instance==null){
            instance = new AuthService();
        }
        return instance;
    }
    private static Employee loggedUser;

    //------------Stock l'utilisateur connecté
    public static void setLoggedUser(Employee employee) {
        loggedUser = employee;
    }
    //--------------Récupère l'utilisateur actuel
    public static Employee getLoggedUser() {
        return loggedUser;
    }
    //--------------Vérification mode Admin
    public static boolean isAdmin() {
        return loggedUser != null && loggedUser.getAdmin();
    }
    //------------Déconnecte l'utilisateur
    public static void logout() {
        AdminController.getInstance().setAdminButton(false);
        loggedUser = null;
    }
}

