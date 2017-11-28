package dimdoors.common.schematic;

public class InvalidSchematicException extends Exception {

	private static final long serialVersionUID = -1011044077455149932L;

	public InvalidSchematicException()
	{
		super();
	}
	
	public InvalidSchematicException(String message)
	{
		super(message);
	}
	
	public InvalidSchematicException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
