package szorgalmi1;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class StorageRunner {

	public static void main(String[] args) {
		

		Scanner input = new Scanner(System.in);
		userInterface(input);
		input.close();

	}

	private static void userInterface(Scanner input) {
		Product.initialize(); // megfeleltet�si lista felt�lt�se
		File file = new File("REK.DAT");
		try {
			if (file.createNewFile()) {
				System.out.println("File created: " + file.getName());
			} else {
				System.out.println("File already exists.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		int choice;
		do {
			System.out.println(
					"\nChoose an operation: \n1 Add products\n2 Take out products\n3 List product stock info\n4 List all data\n5 Exit");
			do {
				while (!input.hasNextInt()) {
					System.out.println("Enter a number between 1 and 5.");
				}
				choice = input.nextInt();
				input.nextLine();

			} while (choice < 1 || choice > 5);
			switch (choice) {
			case 1:
				handleInsertion(input, file);
				break;
			case 2:
				handleDeletion(input, file);
				break;
			case 3:
				handleListing(input, file);
				break;
			case 4:
				readFile(file);
			}
		} while (choice != 5);

		Product.save(); // megfeleltet�si lista lement�se

	}

	private static void handleListing(Scanner input, File file) {
		System.out.println("Name of product: ");
		listProductStock(file, input.nextLine());

	}
	
	private static Compartment compartmentIn(Scanner input, File file, String s1) {
		String in;
		
		String[] splitin;
		boolean ok = false;
		Compartment c = null;
		do {
			System.out
					.println(s1);
			in = input.nextLine();
			splitin = in.split(" ");
			try {
				c = new Compartment(Integer.parseInt(splitin[0]), Integer.parseInt(splitin[1]),
						Integer.parseInt(splitin[2]), Integer.parseInt(splitin[3]));
				ok = true;
			} catch (Exception exc) {
				System.out.println("Enter four numbers separated by spaces.");
			}

		} while (!ok);
		
		return c;
	}
	
	private static int numberIn(Scanner input, File file) {
		int number = 0;
		String in;
		boolean ok = false;
		do {
			System.out.println("Quantity:");
			in = input.nextLine();
			try {
				number = Integer.parseInt(in);
				if (number > 0) {
					ok = true;
				}
			} catch (Exception exc) {
				System.out.println("Enter a positive number.");
			}

		} while (!ok);
		return number;
	}

	private static void handleDeletion(Scanner input, File file) {
		Compartment c = compartmentIn(input, file, "From which compartment would you like to take out? (format: building row column storey)");
		System.out.println("Name of product to take out: ");
		String name = input.nextLine();
		int number = numberIn(input, file);

		takeOutProduct(file, c, name, number);

	}

	private static void handleInsertion(Scanner input, File file) {
		Compartment c = compartmentIn(input, file, "Which compartment would you like to add to? (format: building row column storey)");
		System.out.println("Name of product to add: ");
		String name = input.nextLine();
		int number = numberIn(input, file);

		insertProduct(file, c, name, number);

	}

	private static void takeOutProduct(File file, Compartment c, String name, int number) {
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
			int id = Product.convertToId(name);
			if (id == -2) {
				throw new Exception("No such product in inventory.");
			}
			int currentId, temp;
			boolean done = false;
			long pos = -1;
			System.out.println(raf.length() / (24 * Integer.SIZE / 8));
			for (int i = 0; i < raf.length() / (24 * Integer.SIZE / 8); i++) {
				raf.seek(i * 24 * Integer.SIZE / 8);
				if (raf.readInt() == c.getBuilding() && raf.readInt() == c.getRow() && raf.readInt() == c.getColumn()
						&& raf.readInt() == c.getStorey()) {
					pos = raf.getFilePointer();
					break;
				}
			}
			if (pos == -1) {
				throw new Exception("The specified compartment does not exist.");
			}
			for (int i = 0; i < 10; i++) {
				currentId = raf.readInt();
				if (id == currentId) {
					pos = raf.getFilePointer();
					temp = raf.readInt();
					if (temp < number) {
						throw new Exception(
								"Not enough (" + temp + ") products in the compartment, operation unsuccessful");
					}
					temp -= number;
					if (temp == 0) {
						raf.seek(pos - Integer.SIZE / 8);
						raf.writeInt(-1);
					}
					raf.seek(pos);
					raf.writeInt(temp);
					done = true;
					break;
				}
				raf.readInt();
			}
			if (!done) {
				throw new Exception("There is no stock of the given item in the given compartment.");
			}
			System.out.println("Item successfully removed.");
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
		}

	}

	private static void insertProduct(File file, Compartment c, String name, int number) {
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
			int id = Product.adderConvertToId(name);
			int currentId, temp;
			boolean done = false;
			long pos = -1;
			for (int i = 0; i < raf.length() / (24 * Integer.SIZE / 8); i++) {
				raf.seek(i * 24 * Integer.SIZE / 8);
				if (raf.readInt() == c.getBuilding() && raf.readInt() == c.getRow() && raf.readInt() == c.getColumn()
						&& raf.readInt() == c.getStorey()) {
					pos = raf.getFilePointer();
					break;
				}
			}
			if (pos == -1) {
				throw new Exception("The specified compartment does not exist.");
			}
			for (int i = 0; i < 10; i++) {
				currentId = raf.readInt();
				if (id == currentId) {
					pos = raf.getFilePointer();
					temp = number + raf.readInt();
					raf.seek(pos);
					raf.writeInt(temp);
					done = true;
					break;
				}
				raf.readInt();
			}
			raf.seek(pos);
			for (int i = 0; i < 10; i++) {
				pos = raf.getFilePointer();
				currentId = raf.readInt();
				if (currentId == -1) {
					raf.writeInt(number);
					raf.seek(pos);
					raf.writeInt(id);
					done = true;
					break;
				}
				raf.readInt();
			}
			if (!done) {
				throw new Exception("No place left for the given item.");
			}
			System.out.println("Item successfully added.");
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
		}

	}

	private static void listProductStock(File file, String name) {
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			int current;
			long pos1, pos2;
			boolean isStock = false;
			System.out.println("Stock of " + name + ":");
			for (int i = 0; i < raf.length() / (Integer.SIZE / 8 * 24); i++) {
				pos1 = raf.getFilePointer();
				raf.skipBytes(Integer.SIZE / 8 * 4);
				for (int j = 0; j < 10; j++) {
					current = raf.readInt();
					if (current == Product.convertToId(name)) {
						pos2 = raf.getFilePointer();
						raf.seek(pos1);
						System.out.print(
								raf.readInt() + " " + raf.readInt() + " " + raf.readInt() + " " + raf.readInt() + ": ");
						raf.seek(pos2);
						System.out.println(raf.readInt() + " pieces");
						raf.seek(pos1 + Integer.SIZE / 8 * 24);
						isStock = true;
						break;
					}
					current = raf.readInt();

				}
			}
			if (!isStock) {
				System.out.println("No stock of specified item.");
			}
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
		}

	}

	private static void readFile(File file) {
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			int id;
			for (int i = 0; i < raf.length() / (24 * Integer.SIZE / 8); i++) {
				System.out.print(raf.readInt() + " ");
				System.out.print(raf.readInt() + " ");
				System.out.print(raf.readInt() + " ");
				System.out.print(raf.readInt() + " ");
				for (int j = 0; j < 10; j++) {
					id = raf.readInt();
					System.out.print(Product.convertToName(id) + " ");
					System.out.print(raf.readInt() + ", ");

				}
				System.out.println();
			}
			raf.close();
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
		}
	}

	/*private static void writeToFile(File file, Compartment[] comp) {
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			Product product;
			raf.seek(raf.length());
			for (int i = 0; i < comp.length; i++) {
				raf.writeInt(comp[i].getBuilding());
				raf.writeInt(comp[i].getRow());
				raf.writeInt(comp[i].getColumn());
				raf.writeInt(comp[i].getStorey());
				for (int j = 0; j < 10; j++) {
					product = comp[i].getProductListIndex(j);
					raf.writeInt(product.getId());
					raf.writeInt(product.getNumber());

				}
			}
			raf.close();
		} catch (Exception exc) {
			System.out.println(exc.getMessage());
		}

	}*/

}
