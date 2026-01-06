# Buffer Overflows

A **buffer overflow** is a vulnerability that ahs been found in the heartbeat protocol implementation of TLS (Transport Layer Security) and DTLS (Datagram Transport Layer Security) of OpenSSl. OpenSSL replies a requested amount up to **64 kB of random memory content** as a response to a heartbeat request. Sensitive data such as message contents, **user credentials**, **session keys and server private keys** have been observed within the reply contents. More memory contents can be obtained by sending multiple requests. The attacks have **not been observed to leave traces in application logs**.

![alt text](images/bufferOverflowHeartbeat.png)

## What is a Buffer Overflow?

In a C programs stores data in 4 different memory segments:

- **Stack** - local variables and function call management
- **Heap** - dynamically allocated memory (malloc, new)
- **Data segment** - global and static variables
- **BSS segment** - uninitialized global and static variables

A **buffer** consists in a contiguous block of memory that holds multiple instances of the same data type. A **buffer overflow** occurs when a program writes (or reads) outside the allocated space for the buffer, **normally after** the end (bit **can also occur before** the beginning of the buffer).

## Cause

Languages like C and C++ does not perform **automatic bounds checking** on arrays and buffers. This means that if a program writes more data to a buffer than it can hold, the excess data will overwrite adjacent memory locations. This can lead to **undefined behavior**, crashes, or security vulnerabilities. Besides that, **programers usually make assumptions** like "the user never types more than 1000 characters as input", which can lead to buffer overflows if the user provides more data than expected. 

## What does a Buffer Overflow do?

When a program has a buffer overflow vulnerability, the program becomes unstable, crashes, or behaves aparently normally. This leads to side effects depending on:

- How much data is written beyond the buffer's boundaries.
- What data is overwritten (e.g., control structures, function pointers, return addresses).
- Whether the program tries ti read overwritten data.
- What data ends up replacing the memory that gets overwritten.

## Why are Buffer Overflows a Security Problem?

BOs in the worse case, can be exploited intentionally and let the attacker **execute its own code** on the target system. The main obective is usually to run code with superuser privileges, that is easy if the server runnig with superuser previliges or the attcker can use a **privilege escalation** exploit after gaining access to the system.

**Other ways where Buffer Overflows can cause security problems:**

- **Code might be modified**: Overwriting executable code in memory can change program behavior, allowing attackers to alter the program's logic or inject malicious functionality.
- **Data may be altered**: Buffer overflows can overwrite critical data structures (e.g., function pointers, configuration values, security flags), changing program behavior and potentially bypassing security checks.
- **Data might be read**: Creating an **information leak** where sensitive data (passwords, encryption keys, personal information) stored in adjacent memory can be read by the attacker, compromising confidentiality even without executing malicious code.

A simple way to prevent this kind of attacks is to allways check the size of the input data before copying it to a buffer.

![alt text](images/bufferOverflowSolutions.png)

## Overflowing the Heap and Stack

The memory virtualization is typically solved using **segmentation** and **pagination** mechanisms (80x86 processors support both).

A program stores data in several memory segments:

- **Global variables** - data/bss segments.
- **Local variables** - stack segment.
- **Dynamically allocated memory** - heap segment.

![alt text](images/memorySegments.png)

# Heap Overflow

A **heap overflow** occurs when a program writes more data to a dynamically allocated buffer in the heap than it can hold, overwriting adjacent memory regions.

## Basic Heap Overflow Attack

**Attack mechanism:**

1. **Modify value of data in the heap**: An attacker can overflow a buffer (e.g., `str`) to overwrite data stored in adjacent heap memory (e.g., a `critical` variable).

2. **Example scenario:**
   ```c
   char *str = (char *)malloc(7);
   char *critical = (char *)malloc(7);
   strcpy(critical, "secret");
   strcpy(str, argv[1]); // vulnerability - no bounds checking
   ```

![alt text](images/heapOverflow1.png)

3. **Memory organization in heap**: Both `str` and `critical` are allocated sequentially in the heap. If `str` receives more than 7 bytes of input, it will overflow into the memory space of `critical`, overwriting its content.

![alt text](images/heapOverflow2.png)

## Limitations of Heap Overflow Attacks

**Important constraint:** Although heap overflow attacks can be significant, they have limitations:

- We are **limited to write to higher memory zones** than the buffer (overflow moves forward in memory)
- We **probably cannot write too far above the buffer** because:
  - We need to overwrite the **whole memory in between** the buffer and the target
  - There might be **unallocated memory pages** in between, which would cause the program to crash with a segmentation fault before reaching the target
  
This means heap overflows are typically effective only when the target data structure is located immediately after the vulnerable buffer in memory.

# Stack Overflow

The most common type of Stack Overflow attack is the **stack smashing attack**.

```c
void test(char *s) {
    char buf[10]; //Note: gcc may store extra space
    strcpy(buf, s); //does not check buffer’s limit
    printf(" s = %p\n &buf[0] = %p\n\n", s, buf);
}
main(int argc, char **argv) {
    test(argv[1]);
}
```

**The code is obviously vulnerable:** It inserts untrusted input into a buffer without checking the size, allowing an attacker to overflow the buffer and potentially overwrite adjacent memory.

## Stack Organization and Assembly

When **gcc compiles this code**, it first translates it to **assembly language**, which reveals how the stack is organized and where vulnerabilities can be exploited.

**Key x86-64 Registers (AT&T notation):**

- **RSP (Stack Pointer)**: Points to the top of the stack.
- **RBP (Base Pointer)**: Points to the beginning of the current stack frame.
- **RIP (Instruction Pointer)**: Points to the next instruction to execute; holds the return address.

**Stack Frame Structure:**

When `test()` is called, the stack is organized as follows (stack grows downward in memory):

```
[Higher memory addresses]
    argv[1] = s          ← argument passed to function
    ret address          ← return address (saved RIP)
    saved rbp            ← saved base pointer from caller
    buf[10]              ← local buffer (overflow starts here)
[Lower memory addresses] ← RSP points here
```

**The Attack:**

1. The `strcpy(buf, s)` copies `argv[1]` into `buf` without bounds checking
2. If `argv[1]` is longer than 10 bytes, it overflows `buf`
3. The overflow can overwrite:
   - **saved rbp** (saved base pointer)
   - **ret address** (return address) - **Critical target!**
4. By overwriting the return address, an attacker can redirect execution to arbitrary code when the function returns

**Review of Assembly (64-bit x86; AT&T notation)**

![alt text](images/stackOverflowAssembly.png)
![alt text](images/stackOverflowAssembly2.png)

**Review of Assembly (32-bit x86; AT&T notation)**

![alt text](images/stackOverflowAssembly32.png)
![alt text](images/stackOverflowAssembly32_2.png)

**Other Relevant Info**

![alt text](images/stackOverflowInfo.png)

## Stack Overflow (III): Running the Example

- Supplying a short input (e.g., `12345`) overflows `buf` and overwrites saved `rbp` and part of the return address, but may still return and print values
- Supplying a longer input (e.g., `12345678901`) overflows further and corrupts control data, typically causing a segmentation fault (core dump) when returning
- Key takeaway: overflow grows upward in the stack frame (toward saved `rbp` and saved `rip`)

![alt text](images/stackOverflowExample.png)

## Stack Overflow (IV): Redirecting Execution

- Example introduces a `cannot()` function that should not run
- By placing the address of `cannot` over the saved return address, the function returns into `cannot`, proving control-flow redirection
- Demonstrates how overwriting the saved `rip` transfers control after `test` returns

![alt text](images/stackOverflowRedirect.png)

## Stack Overflow (V): Crafting an Exploit Payload

- Build an input buffer that fills `buf`, overwrites saved `rbp`, and writes the target address into the saved return slot
- Example payload: array with the address of `./stack_example_2`, followed by padding and the target address; `execve` runs the program with that crafted argv
- Result: when `test` returns, execution jumps to the injected address (e.g., `cannot`) instead of the legitimate caller

![alt text](images/stackOverflowExploit.png)

## Stack Layout

**64-bit x86 Stack Layout**
![alt text](images/stackLayout.png)

**32-bit x86 Stack Layout**
![alt text](images/stackLayout32.png)

## Pratical Aspects

In order to find out the place of the return address that has to be overwritten by the BO whithout the source code, the attacker goes by trial and error or reverses engineering the code. 

## Code Injection

### x86-64 

In Unix, the code to span a shell is:

```c
char *args[] = {"/bin/sh", NULL};
execve("/bin/sh", args, NULL};
        1           2      3
```

The corresponding assembly code is:

```assembly
xor %rax, %rax ; %rax = 0
3 movq %rax, %rdx ; %rdx = envp = NULL
2 movq $address_of_argv, %rcx ; %rcx = args
1 movq $address_of_path_string, %rbx ; %rbx = prog
movq %rcx, %rsi ; %rsi = %rcx = args
movq %rbx, %rdi ; %rdi = %rdx = prog
movq $0x3b, %rax ; syscall number for execve()
syscall ; do syscall
```

### x86-32

In Unix, the code to span a shell is:

```c
char *args[] = {"/bin/sh", NULL};
execve("/bin/sh", args, NULL};
        1           2      3
```

The corresponding assembly code is:

```assembly
xor %eax, %eax ; %eax = 0
3 movl %eax, %edx ; %edx = envp = NULL
2 movl $address_of_argv, %ecx ; %ecx = args
1 movl $address_of_path_string, %ebx ; %ebx = prog
movl $0x0b, %al ; syscall number for execve()
int $0x80 ; do syscall
```

## Internal Buffer Overflows

BO can also occur not only in the user code, but also in the buffers of any module linked to the program, such as a library function.

**Example:** `char *realpath(const char *path, char *out_path)`

- Converts a relative path to the equivalent absolute path.
- Problem: output string may be longer than the buffer provided.
- Even if the size of `out_path` is `MAXPATHLEN`, an internal buffer could be overrun.

**Other functions with similar problems:** `syslog`, `getopt`, `getpass`, ... (NOTE: Current implementations of these functions most probably no longer contain these problems!)

## Other Risks

Even "safe" versions of lib calls can be misused, for example, `strncpy()` has typically an undelined behavior if the two buffers overlap or if the original string is larger than the destination buffer this function does not null-terminate the destination string.

## Main Solutions for Protection

1. **Address Space Layout Randomization (ASLR)** - the starting address of the address space segments changes in each execution.
2. **Data Execution Prevention (DEP)**  (also called W Ꚛ X, where W = write; X = execute) - the stack pages cannot be executed, but only read/written, and the code segment can be executed, but not written.
3. **Canaries** - put special (nondeterministic) values (canaries) before (or after) the places we want to protect in memory, and check that they have not benn changed before accessing the protected memory.

## Stack Canaries

The **Stack Canaries** are normally active by default in most sytems; they place a canary (random value) right before the stored RBP and RIP, to detect buffer overflows on the stack attempting to corrupt the RIP to change the execution flow.

**x86-64**

![alt text](images/canary_x86-64.png)

**x86-32**

![alt text](images/canary_x86-32.png)

## Control Flow Integrity (CFI)

The CFI **restricts the control-flow** of an application to valid execution traces only, and enforces this property by **monitoring the program at runtime** and comparing its state to a **set of precomputed valid states**. If an invalid state is detected, an alert is rasied and the program is terminated.

A **control-flow hijack attack** redirects the control-flow of an application to locations that could not be reached in a benign execution of the program. Example, to the injected code or to code that is reused in an alternate context. The CFI can detect these attacks by **limiting the targets of control-flow transfers**.

Depending on the implementation, some techniques can bypass CFI:

- Find an indirect call that was not protected by CFI (either CALL or JMP).
- Use a controlled-write primitive to overwrite an address on the stack (since the stack is not often protected by CFI).
- Set the destination to the code located in a non-CFI module loaded in the same process.

# Advanced Overflow

Shell code includes zeros: 

- Functions like `strcpy()` stop processing when the first zero is reached.
- **Substitute** places where zeros appear **with equivalent code**.
- Example: `mov $0, %eax` can be replaced with `xor %eax, %eax` (sets `%eax` to zero without using a zero byte).

The Lack of space: **reduce code** or **provide it at an earlier time** so that it is available when needed (e.g., environment variables) or **look for the code** in the program/libraries.

Discover the address where the code is injected: for example, the return address must be superseded with this address; Use first an **information leak vulnerability** to get data that allows the calculation of this address.

Escape several forms of protection (e.g., non-executable stack; stack canaries)

## Arc Injection or Return-to-libc

**Difficulty:** The stack **cannot** be executed.
**Assume:** **no** stack canaries or ASLR.