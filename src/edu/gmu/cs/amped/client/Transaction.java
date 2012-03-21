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

import java.text.SimpleDateFormat;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Encapsulates a single message and ID bundle corresponding
 * to some occurrence requiring approval. A transaction may be signed or
 * without a signature. It can be in any state specified in the enum Status.
 * 
 * Transaction text and identifiers are immutable; in order to modify either
 * a new Transaction object must be created. 
 * 
 * @author luke@faraone.cc (Luke Faraone)
 *
 */
public class Transaction {
	private int id;
	private String text;
	private String encrypted;
	private String signature;
	private Status status;
	/**
	 * @return the transaction text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @return the transaction ID, which should be the primary key of the
	 * 		   transaction on the server.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Creates a new Transaction object with the specified transaction ID and 
	 * transaction text.  
	 * @param id A positive integer specifying the primary key of the 
	 * 			 transaction in the transaction database. This is usually
	 * 			 specified by the server.
	 * @param text The text of the transaction as it should be displayed
	 * 			   to the user.
	 */
	public Transaction(int id, String text, Status status ) {
		if (id < 0) {
			// TODO: raise an exception
		}
		this.id = id; 
		this.text = text;
		this.status = status;
	}
	
	/**
	 * Creates a new Transaction object using data extracted from the 
	 * specified JSON
	 * @param jsonBlob JSON in the format specified by the specification.
	 */
	public Transaction(String json) {
		JsonParser parser = new JsonParser();
	    JsonArray array = parser.parse(json).getAsJsonArray();
	    JsonObject object = (JsonObject) array.get(0);
	    JsonObject fields = (JsonObject) object.get("fields");
		this.id = object.get("pk").getAsInt(); 
		this.text = fields.get("text").getAsString();
		this.status = Status.values()[fields.get("status").getAsInt()];
		new SimpleDateFormat(fields.get("attempt_date").getAsString());
	}
	
	/**
	 * @return a String comprising a Keyczar-signed copy of the transaction.
	 * 
	 * Note that this method does not do any verification; it is up to you 
	 * to ensure the signature is accurate.
	 */
	public String getSignature() {
		return this.signature;
	}
	
	/**
	 * @param sig Stored as the new signature for this transaction.
	 */
	public void setSignature(String sig) {
		this.signature = sig;
		
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String toString() {
		return this.getId() + "|" + this.getText() + "|" + this.getStatus().ordinal();
	}
	public String getEncrypted() {
		return encrypted;
	}
	public void setEncrypted(String encrypted) {
		this.encrypted = encrypted;
	}
	
}
