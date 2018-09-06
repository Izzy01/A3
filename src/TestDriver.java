/**
* Author: Ilnaz Daghighian
* 
* TestDriver
* public class TestDriver
* The TestDriver class tests all of the functions of the BufferManager
*/
public class TestDriver {

	public static void main(String[] args) {
		
		BufferManager b = new BufferManager();
		MemoryBlock [] usedMemory = b.getUsedMemory(); //for checking indexing of used memory array
		
		//FIRST TEST
		System.out.println("***Checking on initial status of Buffer Manager***\n");
		System.out.println("Expected Values: NOT tight and 10 (510 word buffers)");
		System.out.println("Actual Values: ");
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains();
//		b.elementsInFreeLists();
		System.out.println("\n");
		
		//SECOND TEST
		System.out.println("***Requesting an illegal size buffer(700)***\n");
		System.out.println("Expected Values: return value of -2, NOT tight and 10 (510 word buffers)");
		System.out.println("Actual Values: " + b.allocateMemory(700));
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains();
		System.out.println("\n");
		
		//THIRD TEST
		System.out.println("***Requsting a 6 word buffer and verifying that it has been allocated***\n");
		System.out.println("Expected Values: NOT tight, 9 (510 word buffers) and 1 (of each other size)");
		System.out.println("Memory block of requested size 6 allocated starting at index: " + b.allocateMemory(6));
		System.out.println("Actual Values: ");
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains(); 
//		b.elementsInFreeLists();
		System.out.println("\n");
		
		//FOURTH TEST 
		System.out.println("***Returning the 6 word buffer and verifying that it has been deallocated***\n");
		System.out.println("Expected Values: NOT tight, 10 (510 word buffers) and 0 (of each other size)");
		b.deallocateMemory(0);
		System.out.println("Actual Values: ");
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains(); 
//		b.elementsInFreeLists();
		System.out.println("\n");
		
		//FIFTH TEST
		System.out.println("***Requesting 10 (510 word buffers) and verifying status***\n");
		System.out.println("Expected Values: Tight, 0 (510 word buffers) and 0 (of each other size)");
			for(int i = 0; i < 10; i++){
				System.out.println("Memory block of requested size 510 allocated starting at index: " + b.allocateMemory(510));
			}
		System.out.println("Actual Values: ");
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains(); 
//		b.elementsInFreeLists();
		System.out.println("\n");
		
		//SIXTH TEST
		System.out.println("***Requesting another (510 word buffer) and verifying status***\n");
		System.out.println("Expected Values: return value of -1, Tight, 0 (510 word buffers) and 0 (of each other size) ");
		System.out.println("Actual Values: " + b.allocateMemory(510));
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains(); 
		System.out.println("\n");
		
		//SEVENTH TEST
		System.out.println("***Returning All word buffers and verifying that they have been deallocated***\n");
		System.out.println("Expected Values: NOT tight, 10 (510 word buffers) and 0 (of each other size)");
			for (int i = 0; i < 5120; i = i + 512){
				b.deallocateMemory(i);
			}	
		System.out.println("Actual Values: ");
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains(); 
//		b.elementsInFreeLists();
		System.out.println("\n");
		
		
		//EIGHTH TEST
		System.out.println("***Checking that no improper combining of non-buddy buffers occur***\n"
				+ "PART 1: First will allocate 4 (254 word buffers)\n");
		System.out.println("Expected Values: NOT tight, 8 (510 word buffers) and 0 (of each other size)");
			for(int i = 0; i < 4; i++){
				System.out.println("Memory block of requested size 254 allocated starting at index: " + b.allocateMemory(254));
			}
		System.out.println("Actual Values: ");
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains();

		System.out.println("\nPART 2: Will return (254 word buffer) starting at index 0 belonging to first 512 word block "
				+ "and will return (254 word buffer) starting at index 769 belonging to second 512 word block\n");
		System.out.println("Expected Values: NOT tight, 8 (510 word buffers) 2 (254 word buffers) 0 (of each other size)");	
		b.deallocateMemory(0);
		b.deallocateMemory(769);
		System.out.println("Actual Values: ");
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains();
		System.out.println("\nStatistics on each element currently in the free lists: ");
		b.elementsInFreeLists();
		System.out.println("\n");

		//NINTH TEST
		System.out.println("***Checking that proper combining of buddy buffers occur***\n"
				+ "Part 1 : First will return (254 word buffer) starting at index 257 belonging to first 512 word block\n");
		System.out.println("Expected Values: NOT tight, 9 (510 word buffers) 1 (254 word buffer) and 0 (of each other size)");
		b.deallocateMemory(257);
		System.out.println("Actual Values: ");
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains();
		System.out.println("\nStatistics on each element currently in the free lists: ");
		b.elementsInFreeLists();
		
		System.out.println("\nPART 2: Will return (254 word buffer) starting at index 512 belonging to second 512 word block");
		System.out.println("Expected Values: NOT tight, 10 (510 word buffers) and 0 (of each other size)");	
		b.deallocateMemory(512);
		System.out.println("Actual Values: ");
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains();
		System.out.println("\nStatistics on each element currently in the free lists: ");
		b.elementsInFreeLists();
		System.out.println("\n");
		
		
		//TENTH TEST
		System.out.println("***Requesting that a buffer that is not used be returned/freed -will try to free block at starting index 0***\n");
		System.out.println("Used memory stats for index 0: " + usedMemory[0]);
		System.out.println("\nExpected Values: Print statement: There is no memory block to be freed at this address");
		System.out.println("Actual Values: ");
		b.deallocateMemory(0);
		System.out.println("\n");
		
		
		//ELEVENTH TEST 
		System.out.println("***Checking deep splitting and merging***\n"
				+ "Part 1 : Will request 4 (6 word buffers)\n");
		System.out.println("Expected Values: NOT tight, 9 (510 word buffers) and 1 of each (30, 62, 126, 254 word buffers)");
			for(int i = 0; i < 4; i++){
				System.out.println("Memory block of requested size 6 allocated starting at index: " + b.allocateMemory(6));
			}
		System.out.println("Actual Values: ");
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains();
		b.elementsInFreeLists();
		
		System.out.println("\nPART 2: Will return 2 (6 word buffers) starting at index 1024 and 1033");
		System.out.println("Expected Values: NOT tight, 9 (510 word buffers) and 1 of each (14, 30, 62, 126, 254 word buffers)");	
		b.deallocateMemory(1024);
		b.deallocateMemory(1033);
		System.out.println("Actual Values: ");
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains();
		b.elementsInFreeLists();
		
		System.out.println("\nPART 3: Will return 2 (6 word buffers) starting at index 1041 and 1050");
		System.out.println("Expected Values: NOT tight, 10 (510 word buffers) and 0 (of each other size)");	
		b.deallocateMemory(1041);
		b.deallocateMemory(1050);
		System.out.println("Actual Values: ");
		System.out.println("Buffer Pool is Tight: " + b.statusOfBufferPool());
		b.numberOfBuffersInChains();
		b.elementsInFreeLists();

		
				
	}//end main
}//end driver
