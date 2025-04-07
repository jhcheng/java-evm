### **1. Core Specifications**
- **[Ethereum Yellow Paper]**  
  The formal mathematical specification defining EVM architecture, gas mechanics, and execution environment. Essential for understanding opcodes, memory models, and state transitions.

- **[Ethereum Improvement Proposals (EIPs)]**  
  Critical EIPs like [EIP-1559 (Fee Market)](https://eips.ethereum.org/EIPS/eip-1559) and [EIP-4844 (Proto-Danksharding)](https://eips.ethereum.org/EIPS/eip-4844) introduce EVM-level changes. Study EIPs tagged with "Core" or "Networking".

---

### **2. Reference Implementations**
- **Go-Ethereum (Geth) EVM Codebase**  
  Study Geth's [`core/vm`](https://github.com/ethereum/go-ethereum/tree/master/core/vm) module for a production-grade Go implementation.

- **Py-EVM**  
  A Python implementation ([GitHub](https://github.com/ethereum/py-evm)) ideal for learning due to its readability and modular design.

---

### **3. Developer Guides**
- **[EVM Opcodes Handbook](https://www.evm.codes/)**  
  Interactive reference for all EVM opcodes, gas costs, and stack behaviors.

- **[Building a Minimal EVM](https://github.com/ethereum/evmone)**  
  Use evmone (C++17 EVM implementation) as a template for building lightweight EVMs.

---

### **4. Testing Tools**
- **Ethereum Tests**  
  Run official test vectors from [`ethereum/tests`](https://github.com/ethereum/tests) to validate your implementation against consensus rules.

- **Revm**  
  A Rust EVM with benchmarking capabilities ([GitHub](https://github.com/bluealloy/revm)), useful for optimizing gas calculations.

---

### **5. Advanced Topics**
- **Ethereum Execution Specs**  
  Detailed [JSON-based specs](https://github.com/ethereum/execution-specs) for state transitions and precompiled contracts.

- **EVM Parallelization Research**  
  Explore proposals like [EIP-648](https://eips.ethereum.org/EIPS/eip-648) to implement parallel transaction processing.

---

**Recommendation**: Start with Py-EVM for prototyping, then cross-validate using Geth's implementation. Use the Yellow Paper as your primary reference for edge cases.

https://github.com/ethereum/go-ethereum/tree/master/core/vm

https://github.com/ethereum/py-evm

https://www.nico.bio/articles/evm-from-scratch

https://karmacoma.notion.site/Building-an-EVM-from-scratch-part-1-the-execution-context-c28ebb4200c94f6fb75948a5feffc686

