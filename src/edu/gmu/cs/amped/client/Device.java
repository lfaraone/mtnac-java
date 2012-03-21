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

package edu.gmu.cs.amped.client;

import org.keyczar.*;
import org.keyczar.exceptions.KeyczarException;

/**
 * An entity communicating with the server which has two 
 * asymmetric keypairs used to sign/encrypt a {@link Transaction}.
 * 
 * @author luke@faraone.cc (Luke Faraone)
 *
 */
public class Device {
	private int id;
	private Crypter myCrypter;

	/**
	 * Initializes a new Device object.
	 *  
	 * @param deviceId unique positive integer that represents the device in the server
	 * @param deviceSigner Keyczar public/private keypair used for signing
	 * @param crypter Keyczar public/private keypair used for encryption
	 */
	public Device(int deviceId, Signer deviceSigner, Crypter crypter) {
		this.id = deviceId;
		this.myCrypter = crypter;
	}

	/**
	 * @return the id uniquely identifying the Device with a Server
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Decrypts transaction JSON and returns there resulting object. 
	 * @param data encrypted JSON data received from the server
	 * @return a Transaction object corresponding to the decrypted data.
	 * @throws KeyczarException if the decryption fails, eg. if the data is 
	 * 		   malformed or otherwise not decryptable.
	 */
	public Transaction decryptTransaction(String data) throws KeyczarException {
		String decryptedData = myCrypter.decrypt(data);
		return new Transaction(decryptedData);
	}

}
