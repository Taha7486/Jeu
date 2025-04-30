import java.util.ArrayList;
import java.util.List;

public class Ensemble {
    private int []T;

    private ArrayList<Integer> listeEtudiant2;

    public Ensemble(ArrayList<Integer> listeEtudiant2, int[] t) {
        this.listeEtudiant2 = listeEtudiant2;
        listeEtudiant2=new ArrayList<>();
        T = t;
    }

    public int[] getT() {
        return T;
    }

    public void setT(int[] t) {
        T = t;
    }
public ArrayList<Integer> getListeEtudiant2() {
        return listeEtudiant2;
}
public void AjouterNote (){

}

}
