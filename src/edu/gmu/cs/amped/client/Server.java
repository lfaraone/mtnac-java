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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.keyczar.Encrypter;
import org.keyczar.Signer;
import org.keyczar.Verifier;
import org.keyczar.exceptions.KeyczarException;

/**
 * Represents a remote system which speaks the AMPED API.
 * 
 * @author luke@faraone.cc (Luke Faraone)
 *
 */
public class Server {

	private String apiUrl;
	private Verifier serverVerKey;
	private Encrypter serverEncKey;
	
	/**
	 * @return the base URL of the server we're communicating with
	 */
	public String getApiUrl() {
		return apiUrl;
	}

	/** 
	 * Initializes a new Server object. 
	 * @param apiUrl URL that corresponds to the base of the API. Example: http://localhost:8000
	 * @param serverVerifier Verifier which contains the public key of the server
	 */
	public Server(String apiUrl, Verifier serverVerifier, Encrypter serverEncrypter) {
		this.apiUrl = apiUrl;
		this.serverVerKey = serverVerifier;
		this.serverEncKey = serverEncrypter;
	}
	
	/**
	 * Submits the Transaction to the server specified in apiUrl.
	 * 
	 * @param txn Transaction object to send
	 * @param deviceId ID which uniquely identifies the device to the server
	 */
	public void sendTransaction(Transaction txn, int deviceId, Signer deviceSigKey) {
		
		URL modTransaction;
		try {			

			txn.setEncrypted(this.serverEncKey.encrypt(txn.toString()));
			txn.setSignature(deviceSigKey.sign(txn.getEncrypted()));
			
			modTransaction = new URL(this.apiUrl + "/api/txn/+mod");
			HttpURLConnection conn = (HttpURLConnection)modTransaction.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
			osw.write("payload" + "=" + txn.getEncrypted() + 
					"&" + "signature" + "=" + txn.getSignature() +
					"&" + "device_id" + "=" + deviceId);
			osw.flush();
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
				// TODO raise error
				{
				throw new MtnacError("Bad response code");
				}
				
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyczarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Gets the latest transaction from the server for the specified device.
	 * 
	 * @param dev Device whose key information and identifiers are used to
	 * 			  query the server for new transactions
	 * @return a Transaction object corresponding to the latest transaction
	 * 		   modifiable by dev. 
	 * @throws KeyczarException if signature verification fails
	 * @throws IOException 
	 */
	public Transaction getLatestTransaction(Device dev) throws KeyczarException, IOException {
		// TODO: retrieve data
		URL lastTxn = new URL(this.apiUrl + "/api/user/+last-txn?device_id=" + dev.getId());
		String[] parts = new BufferedReader(
		        new InputStreamReader(
		        lastTxn.openStream())).readLine().split("\\|", 2);
		// 0 is encrypted data, 1 is signature
		
		/* So there's a funny story here. 
		 * 
		 * When I was testing this code, I was getting really wonky errors in 
		 * org.keyczar.Crypter.decrypt; as it turns out you are to pass the 
		 * verifier binary data, but data passed to the crypter must be base64-
		 * encoded. This is confusing as hell. 
		 */
		String data = parts[0];
		
		String signature = parts[1];
		
		// this will abort of the signature is invalid, raising KeyczarException
		if (serverVerKey.verify(data, signature) == false) {
			throw new KeyczarException("Invalid Signature");
		}
		/* Historical note:
		 * 
		 * Up until this point, I totally ignored key versioning, et cetera.
		 * This was rather silly of me, and totally caused me to not use some
		 * of the rather useful features of Keyczar. It was possible to hack
		 * around this using UnversionedSigner etc objects, but there does not
		 * appear to be such an UnversionedCrypter. Hmm, I wonder why. 
		 */
		return dev.decryptTransaction(data);
	}
	
	/** 
	 * Checks on the current transaction status with the server specified in apiUrl.
	 * 
	 * @param txnId unique ID of the transaction to check
	 * @return a Status corresponding to the current status of the transaction
	 * 		   with the server.
	 */
	public Status checkTransactionStatus(int txnId) {
		// TODO: return transaction status :)
		URL statusUrl;
		try {
			statusUrl = new URL(this.apiUrl + "/api/txn/" + txnId + "/+stat");

			return Status.values()[new Integer((new BufferedReader(
					new InputStreamReader(
			        statusUrl.openStream()))).readLine().trim())];
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}