package utils;

import model.Employee;

/*----------------Stockage de l'utilisateur en mémoire et vérification mode Admin---------------*/


public class AuthService {
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
        loggedUser = null;
    }
}

