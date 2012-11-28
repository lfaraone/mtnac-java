package edu.gmu.cs.amped.client;
/*
 * Copyright © 2011 Luke Faraone <luke@faraone.cc>, except where noted.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.ini4j.Ini;
import org.keyczar.*;
import org.keyczar.exceptions.KeyczarException;

public class CliClient {

	/**
	 * Determine the proper path for a configuration file, and return it.
	 * We search in these locations:
	 *  - Directory where this program is running
	 *  - ~/.config/mtnac/
	 * @param file the file name with extension
	 * @return the full path to the file
	 */
	public static String getFilePath(String file) {
		// try the directory which this program is running from (not cwd)
		File execDir = (new File(ClassLoader.getSystemClassLoader().getResource(".").getPath())).getParentFile();
		File confFileInExecDir = new File(execDir, file);
		if (confFileInExecDir.exists())
			return confFileInExecDir.getPath();

		String sep = System.getProperty("file.separator");
		return System.getProperty("user.home") + sep + ".config" + sep + "mtnac" + sep + file;

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {


		Scanner in = new Scanner(System.in);

		// to verify server sigs
		Verifier serverVerifier = new Verifier(getFilePath("server_pub"));
		Encrypter serverEncrypter = new Encrypter(getFilePath("server_enc_keys"));
		// to decrypt messages from the server
		Crypter deviceCrypter = new Crypter(getFilePath("device_crypt"));
		// to sign responses to the server
		Signer deviceSigner = new Signer(getFilePath("device_sign"));

		Ini.Section config = (new Ini(new URL("file://" + getFilePath("cli.ini")))).get("DEFAULT");

		Server ser = new Server(config.get("base_url"), serverVerifier, serverEncrypter);
		Device dev = new Device(new Integer(config.get("device_id")), deviceSigner, deviceCrypter);

		System.out.println("Looking for a new transaction...");
		Transaction txn = ser.getLatestTransaction(dev);
		while (txn.getStatus() != Status.UNAUTHENTICATED) {
			System.out.println("No dice, let's wait for one...");
			Thread.sleep(7000);
			txn = ser.getLatestTransaction(dev);
		}

		System.out.print("Message:\n\t" + txn.getText() + "\n");

		while (txn.getStatus() == Status.UNAUTHENTICATED) {
			System.out.println("Do you want to approve this? [y/N]");
			String reply = in.nextLine().trim();
			if (reply.equals("y"))
				txn.setStatus(Status.APPROVED);
			else if (reply.equals("n") || reply.equals(""))
				txn.setStatus(Status.DENIED);
			// TODO ask the user more questions in the above
			else
				System.out.println("Invalid entry.\n");
		}

		ser.sendTransaction(txn, dev.getId(), deviceSigner);
		System.out.println("Status is now " + ser.checkTransactionStatus(txn.getId()));

		} catch (KeyczarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
