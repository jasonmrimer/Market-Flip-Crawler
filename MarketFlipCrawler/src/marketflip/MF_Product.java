package marketflip;

public class MF_Product {
	private String tempProductRepresentation;
	
	public MF_Product(String string) {
		this.setTempProductRepresentation(string);
	}

	public String getTempProductRepresentation() {
		return tempProductRepresentation;
	}

	public void setTempProductRepresentation(String tempProductRepresentation) {
		this.tempProductRepresentation = tempProductRepresentation;
	}

	@Override
	public String toString(){
		return "This product is: " + tempProductRepresentation;
	}
	
}
