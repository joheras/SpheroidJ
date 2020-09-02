package spheroidj;

class Estatico {
	public static int cont = 2;
	public int x;

	public Estatico() {
		this.x = 0;
		Estatico.cont++;
	}

	public static void setContZero() {
		Estatico.cont = 0;
	}
}

public class Principal {
	public static void main(String[] a) {
		Estatico v[] = new Estatico[500];
		System.out.println("El valor de cont: " + Estatico.cont);
		for (int i = 0; i < 500; i++) {
			v[i] = new Estatico();
		}
		System.out.println("El valor de cont: " + Estatico.cont);
		Estatico.setContZero();
		System.out.println("El valor de cont: " + Estatico.cont);
	}
}