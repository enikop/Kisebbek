package szorgalmi1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Product {
	private int id;
	private int number;
	private static ArrayList<ProductCodeDecoder> listOfAllProducts = new ArrayList<ProductCodeDecoder>();
	public static void initialize() {
		File file = new File("termekek.txt");
		try(BufferedReader fbe = new BufferedReader(new FileReader(file))) {
			String line;
			while((line=fbe.readLine())!=null) {
				listOfAllProducts.add(new ProductCodeDecoder(line, listOfAllProducts.size()));
			}
		} catch (IOException e) {
			System.out.println("Az adatbázis nem állt fel megfelelõen, a termekek.txt hiányzik.");
		}	
		
	}
	public static void save() {
		File file = new File("termekek.txt");
		try(BufferedWriter fki = new BufferedWriter(new FileWriter(file, false))) {
			for(int i=0; i<listOfAllProducts.size(); i++) {
				fki.write(listOfAllProducts.get(i).getName()+"\n");
			}
		} catch (IOException e) {
			System.out.println("Sikertelen mentés, a termekek.txt tartalma sérülhetett.");
		}	
		
	}
	public Product(String name, int number) {
		super();
		this.number = number;
		this.id = adderConvertToId(name);
	}
	public Product(int id, int number) {
		super();
		this.number = number;
		this.id = id;
	}
	public static int convertToId(String name) {
		if(name.isEmpty()) {
			return -1; //ha üres mezõ névadata
		}
		for(int i=0; i<listOfAllProducts.size(); i++) {
			if(name.equalsIgnoreCase(listOfAllProducts.get(i).getName())) {
				return listOfAllProducts.get(i).getId();
			}
		}
		return -2; //ha nem üres név, de nincs bent
		
	}
	public static int adderConvertToId(String name) {
		if(name.isEmpty()) {
			return -1;
		}
		for(int i=0; i<listOfAllProducts.size(); i++) {
			if(name.equalsIgnoreCase(listOfAllProducts.get(i).getName())) {
				return listOfAllProducts.get(i).getId();
			}
		}
		listOfAllProducts.add(new ProductCodeDecoder(name, listOfAllProducts.size()));
		return listOfAllProducts.size()-1;	
		
	}
	public static String convertToName(int id) {
		for(int i=0; i<listOfAllProducts.size(); i++) {
			if(id == listOfAllProducts.get(i).getId()) {
				return listOfAllProducts.get(i).getName();
			}
		}
		return null;	
		
	}
	public String getName() {
		return convertToName(id);
	}
	
	public int getId() {
		return id;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public void addToNumber(int add) {
		this.number+=add;
	}
	public static ArrayList<ProductCodeDecoder> getListOfProducts() {
		return listOfAllProducts;
	}
	
	@Override
	public String toString() {
		return "Product [name=" + getName() + ", number=" + number + "]";
	}
	
	
	
	
	
}
