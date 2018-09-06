/**
* Author: Ilnaz Daghighian
* 
* MemoryBlock
* public class MemoryBlock
* The MemoryBlock class represents a memory block object which 
* is used by the BufferManager. It stores the attributes: 
* startAddress, endAddress, bufferSize, and bufferBuddy. 
*/
public class MemoryBlock {
	
	private int startAddress; 
	private int endAddress; 
	private int bufferSize;
	private int bufferBuddy;
	
	public MemoryBlock(int startAddress, int endAddress, int bufferSize, int bufferBuddy) {
		this.startAddress = startAddress;
		this.endAddress = endAddress; 
		this.bufferSize = bufferSize;
		this.bufferBuddy = bufferBuddy;
	}
	
	public int getStartAddress() {
		return startAddress;
	}

	public void setStartAddress(int startAddress) {
		this.startAddress = startAddress;
	}

	public int getEndAddress() {
		return endAddress;
	}

	public void setEndAddress(int endAddress) {
		this.endAddress = endAddress;
	}

	public int getBufferSize() {
		return bufferSize;
	}
	
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	public int getBufferBuddy() {
		return bufferBuddy;
	}
	
	public void setBufferBuddy(int bufferBuddy) {
		this.bufferBuddy = bufferBuddy;
	} 
	
	@Override
	public String toString(){
		return "\nStartAddress: " + startAddress + " EndAddress: " + endAddress + " Buffer Size: " 
				+ bufferSize + " Index of Buddy: " + bufferBuddy;
	}

}//end class
