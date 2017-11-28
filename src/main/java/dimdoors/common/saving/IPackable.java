package dimdoors.common.saving;

public interface IPackable<T>
{
	public String name();
	public T pack();
	public boolean isModified();
	public void clearModified();
}
