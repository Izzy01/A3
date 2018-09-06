import java.util.*;
/**
* Author: Ilnaz Daghighian
* 
* BufferManager
* public class BufferManager  
* The BufferManager class allocates and deallocates memory buffers from a fixes sized area. 
* The available memory is set to 10 of the maximum buffer sizes which are 510 words with 
* 2 words reserved as control words at the end. 
* Buffers are given out in sizes (which are powers of 2 less 2) from 6 words to 510 words. 
* The manager uses an offset from the start (index 0) of the fixed size array to allocate and deallocate memory.
* Requests for illegal block sizes will return an error status (a -2 for illegal request) and a request for more
* memory than is available will return an error status of -1. 
* The manager also has a method that will return a tight(less than 2 maximum sized buffers exist)/not tight status
* of the buffer pool as well as a method that will return the number of buffers in each buffer chain.  
*/

public class BufferManager {
	
	//instance variables 
	private static MemoryBlock[] usedMemory;//used memory array
	private static MemoryBlock noBuddyAddToFreeList;
	private static List<LinkedList<MemoryBlock>> freeList; //free list
	private static LinkedList<MemoryBlock> addList;//add list 
	private static LinkedList<MemoryBlock> size8;
	private static LinkedList<MemoryBlock> size16;
	private static LinkedList<MemoryBlock> size32;
	private static LinkedList<MemoryBlock> size64;
	private static LinkedList<MemoryBlock> size128;
	private static LinkedList<MemoryBlock> size256;
	private static LinkedList<MemoryBlock> size512;
	 
	private static int arrayIndex; //index into freeList 
	private static int numberOf512Blocks; //counter for number of 512 blocks
	
	
	//constructor 
	public BufferManager(){
		usedMemory = new MemoryBlock[5120];
		freeList = new ArrayList<LinkedList<MemoryBlock>>();
		addList = new LinkedList<MemoryBlock>();
		size8 = new LinkedList<MemoryBlock>();
		size16 = new LinkedList<MemoryBlock>();
		size32 = new LinkedList<MemoryBlock>();
		size64 = new LinkedList<MemoryBlock>();
		size128 = new LinkedList<MemoryBlock>();
		size256 = new LinkedList<MemoryBlock>();
		size512 = new LinkedList<MemoryBlock>();
		
		freeList.add(size8);
		freeList.add(size16);
		freeList.add(size32);
		freeList.add(size64);
		freeList.add(size128);
		freeList.add(size256);
		freeList.add(size512);
		
		create10MaxSizeBuffers();
		numberOf512Blocks = 10;
	}
	
	//getters 
	public MemoryBlock[] getUsedMemory() {
		return usedMemory;
	}


	public List<LinkedList<MemoryBlock>> getFreeList() {
		return freeList;
	}
	
	//method to allocate memory block
	public int allocateMemory(int blockSize){
		
		//check for illegal block size... return -2 for illegal request
		if (blockSize > 510 || blockSize < 6 || !powerOf2(blockSize+2)) { 
			return -2; 
		}
	
		//if legal block size call helper method to get correct index into array list of free lists
		int arrayIndex = getFreeListIndex(blockSize + 2);
		
		//while NO memory has been allocated 
		//exit/break; once memory is allocated or there are no free blocks available due to lack of space 
		while(true){
			
			//if list of requested size is NOT empty
			if (freeList.get(arrayIndex) != null && !freeList.get(arrayIndex).isEmpty()) { 
				
				//if requested size is 510 and there are available buffers on this size on freeList 
				//then decrement numberOf512Blocks 
				if(blockSize == 510){
					numberOf512Blocks--;
				}
				//find the first available block from the front of the free list of size provided
				MemoryBlock blockToAllocate = freeList.get(arrayIndex).getFirst();
				//assign it to usedMemory array -the index it will be placed in is at the blocks start address 
				int startAddress = blockToAllocate.getStartAddress();
				usedMemory[startAddress] = blockToAllocate; 
				//remove from linked list since block is not free anymore and return the start address
				freeList.get(arrayIndex).remove(0);
				return startAddress; 
			
			}

			//if list of requested size IS empty
			//continue searching for a free list (linked list) of the next larger size until an available block is found		
		    //or last available block is reached without available space -return -1 if this is the case
			else {
				int checkArray = arrayIndex + 1; 
					if (checkArray > 6){ 
						return -1;
					}
					else {				
						while(checkArray <= 6){	
							// if next larger size list IS empty
							if (freeList.get(checkArray) == null || freeList.get(checkArray).isEmpty()) { 
								checkArray++; 
							}			
							else{
								//if larger size is found split the available block in half 
								//splitBlock method will then add them to the free list of appropriate size 
								//if 512 size block is split splitBlock method will decrement count of numberOf512sizeBlocks
								splitBlock(freeList.get(checkArray).getFirst());
								freeList.get(checkArray).remove(0);//remove block
								break;
							}
						}//end inner while loop 
					}//end inner else 			
			}//end outer else
		}//end outer while loop		
	}//end method allocateMemory
 	
	
	//method to deallocate memory given the starting address (index) of memory block to return
	public void deallocateMemory(int startAddress){
		
		MemoryBlock blockToFree = usedMemory[startAddress]; //the block to free
		
		//there is memory to be freed at given start address/index
		if (blockToFree != null){
			int sizeOfBuffer = blockToFree.getBufferSize() + 2; //size of buffer to free
			arrayIndex = getFreeListIndex(sizeOfBuffer); //index of freeList 
			
			//if block being returned is of max size 510 numberOf512Blocks is incremented
			if(sizeOfBuffer - 2 == 510){
				numberOf512Blocks++;
			}
			
			//if the block is less than 512 in size check if it can be merged 
			//else if it is of size 512 return it to the list of size 512
			if(sizeOfBuffer < 512){
				
				//size of free list to start searching for buddy buffers
				LinkedList<MemoryBlock> freeBuffers = freeList.get(arrayIndex);
				
				//if free list of size to be returned is empty 
				if(freeBuffers.isEmpty() || freeBuffers == null){
					freeList.get(arrayIndex).add(blockToFree);//return to correct size list
	            	usedMemory[startAddress] = null; //set index in used memory to null 
				}
				//else iterate through the free lists
				else {
					//obtain an iterator for freeList indices 
					ListIterator<LinkedList<MemoryBlock>> arrayListIterator = freeList.listIterator(arrayIndex);
						while (arrayListIterator.hasNext()) {
							LinkedList<MemoryBlock> sizeElement = arrayListIterator.next(); //advance to next freeList index
							
							//obtain an iterator for the elements of each size of block 
							ListIterator<MemoryBlock> linkedListIterator = sizeElement.listIterator();
								while (linkedListIterator.hasNext()) {
										MemoryBlock block = linkedListIterator.next();//advance to next block element in current size freeList 
										//if buddy buffer exists
										if(block.getEndAddress() == blockToFree.getBufferBuddy() || block.getStartAddress() == blockToFree.getBufferBuddy() 
												|| block.getBufferBuddy() == blockToFree.getStartAddress()){
											//merge the blockToFree with its buddy 
											//mergeBlock method will merge blocks, remove smaller blocks from usedMemory and increment numberOf512Blocks accordingly)
											blockToFree = mergeBlocks(block, blockToFree); 
											linkedListIterator.remove(); //remove the buddy block from the freeList  
											//keep track of merged blocks in addList
											//when looping completes the last blockToFree added to addList is added to the correct size freeList
											addList.add(blockToFree); 				
										}
										else{
											noBuddyAddToFreeList = blockToFree;//if buddy buffer does not exists add it to this array
										}
								}//end while	
						}
						if(!addList.isEmpty())	{
							//add final merged block to the correct size free list 
							MemoryBlock finalBlockToAdd = addList.getLast();
							int finalBlockArrayIndex = getFreeListIndex(finalBlockToAdd.getBufferSize() + 2); 
							freeList.get(finalBlockArrayIndex).add(finalBlockToAdd);
							addList.clear();//clear list 
						}
						else {
							//add block without buddy buffer to the correct size free list 
							MemoryBlock noBuddy = noBuddyAddToFreeList;
							int noBuddyIndex = getFreeListIndex(noBuddy.getBufferSize() + 2);
							freeList.get(noBuddyIndex).add(noBuddy);
							usedMemory[noBuddy.getStartAddress()] = null; //set index in used memory to null 
							noBuddyAddToFreeList = null; //empty addToFreeList array
						}
				}//end inner else 		
			}//end inner if 
			else {
				//it is of size 512
				size512.add(blockToFree); //return it to the list of size 512
				usedMemory[startAddress] = null; //set index in usedMemory array to null
			}
		}
		else {
			System.out.println("There is no memory block to be freed at this address");
		}	
	}//end method 
	
	//method to check on the buffer pool if tight(less than 2 maximum sized buffer exit) returns true
	public boolean statusOfBufferPool(){
	
		if(numberOf512Blocks < 2){
			return true;
		}
		return false;
		
	}
	
	//method that prints the number of buffers in the buffer chains/free lists
	public void numberOfBuffersInChains(){
		for (int i = 0; i < getFreeList().size(); i++) {
			System.out.println("Buffers of size " + (int)(Math.pow(2,(i+3)) - 2) + ": " 
								+ getFreeList().get(i).size()); 
		
		}
	}
	
	//method added for easier testing/debugging of functions -prints status of each element in the free lists 
	public void elementsInFreeLists(){
		for (int i = 0; i < getFreeList().size(); i++) {
			System.out.println(getFreeList().get(i)); 		
		}
	}
	
	//////////////PRIVATE HELPER METHODS////////////
	
	//method to merge the two blocks accordingly then delete the smaller blocks and return the new larger block
	private MemoryBlock mergeBlocks(MemoryBlock block1, MemoryBlock block2){
		//get smaller block parameters 
		int startAddress1 = block1.getStartAddress();
		int startAddress2 = block2.getStartAddress();
		int endAddress1 = block1.getEndAddress();
		int endAddress2 = block2.getEndAddress();
		int bufferSize = block1.getBufferSize();
		//calculate merged block parameters 
		int newStartAddress = Math.min(startAddress1, startAddress2);
		int newEndAddress = Math.max(endAddress1, endAddress2);
		int newBufferSize = (bufferSize*2) + 2; 
		int newBufferBuddy; 
		if(newBufferSize == 510){
			newBufferBuddy = -1;
		}
		else{
			newBufferBuddy = newEndAddress + 1;
		}
		//create new merged block 
		MemoryBlock mergedBlock = new MemoryBlock(newStartAddress, newEndAddress, newBufferSize, newBufferBuddy);
		
		//if mergedBlock size is 510 numberOf512Blocks is incremented 
		if(mergedBlock.getBufferSize()== 510){
			numberOf512Blocks++;
		}
		
		//remove smaller blocks from used memory
		usedMemory[startAddress1] = null; //set index in usedMemory array to null for block1
		usedMemory[startAddress2] = null; //set index in usedMemory array to null for block2
	
		return mergedBlock;
	}	
		
	//method checks if number given is a power of 2
	private boolean powerOf2(int number){
	     return (number > 0) && ((number & (number - 1)) == 0);
	 }
	
	//method will split provided block of legal size and then add to appropriate size free list
	private void splitBlock(MemoryBlock blockToSplit){
		
		int startAddress = blockToSplit.getStartAddress();
		int endAddress = blockToSplit.getEndAddress();
		int splitSize = (blockToSplit.getBufferSize() + 2) / 2; //buffer size -power of 2
		int bufferSize = splitSize - 2;
		int firstBlockEndAddress = startAddress + splitSize;
		int secondBlockStartAddress = firstBlockEndAddress + 1;
		
		if(bufferSize == 510){
			numberOf512Blocks--;
		}
		 
		//create the split blocks 
		MemoryBlock block1 = new MemoryBlock(startAddress, firstBlockEndAddress, bufferSize, secondBlockStartAddress);
		MemoryBlock block2 = new MemoryBlock(secondBlockStartAddress, endAddress, bufferSize, firstBlockEndAddress);
		
		//get appropriate index for free list
		int arrayIndex = getFreeListIndex(splitSize); 
		
		//add split blocks to appropriate pool size 
		freeList.get(arrayIndex).add(block1); 
		freeList.get(arrayIndex).add(block2);	
		
	}
	
	//method will determine the correct index for the memory pool based on size given
	private int getFreeListIndex(int size){
		 
		switch (size){
		case 8: arrayIndex = 0; break; 
		case 16: arrayIndex = 1; break;
		case 32: arrayIndex = 2; break;
		case 64: arrayIndex = 3; break;
		case 128: arrayIndex = 4; break;
		case 256: arrayIndex = 5; break;
		case 512: arrayIndex = 6; break;
		}
		return arrayIndex;		
	}
	
	//method will create 10 max size buffers of 510 words
	private void create10MaxSizeBuffers(){
		for (int i = 0; i < 5120; i += 512) {
			//startAddress = i, endAddress = i + 511; 
			size512.add(new MemoryBlock(i, (i + 511), 510, -1));			
		}
	}
	
}//end class
