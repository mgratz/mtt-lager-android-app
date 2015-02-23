package de.mtt.lager.android.backend;


import java.util.UUID;


public class BackendRequest {

	private String	uniqueId;


	public BackendRequest() {
		this.uniqueId = UUID.randomUUID().toString();
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BackendRequest other = (BackendRequest) obj;
		if (uniqueId == null) {
			if (other.uniqueId != null) {
				return false;
			}
		} else if (!uniqueId.equals(other.uniqueId)) {
			return false;
		}
		return true;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
		return result;
	}


	@Override
	public String toString() {
		return "BackendRequest [uniqueId=" + uniqueId + "]";
	}
}
