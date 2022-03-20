<script>
  import Table from "./ResultTable.svelte";
  export let userId = "";
  export let numMovies = "";
  export let engine = "";
  export let result;
  export const response = async () => {
    const options = {
      method: "POST",
      mode: "cors",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ id: userId, num: numMovies, engine: engine })
    };
    await fetch("http://localhost:5000/recommend", options)
      .then((res) => res.json())
      .then((res) => {
        if (res) {
          result = res;
        }
      })
      .catch((err) => console.error(err));
  };
</script>

<main>
  <header>
    <h1>Movie Recommendation System</h1>
  </header>
  <input
    type="text"
    id="userId"
    bind:value={userId}
    placeholder="Enter User ID"
  />
  <input
    type="text"
    id="num"
    bind:value={numMovies}
    placeholder="Enter Number of Movies"
  />
  <h3>Select a Recommendation Engine</h3>
  <select bind:value={engine} name="engine" id="engine">
    <option value="item-based">Item Based</option>
    <option value="user-based">User Based</option>
  </select>
  <button type="button" on:click={() => response()}>Submit</button>
  {#if result}
    <Table {...result} />
  {/if}
</main>

<style>
  main {
    text-align: center;
    margin: 0 auto;
  }

  input {
    align-self: center;
    display: block;
    margin-right: auto;
    margin-left: auto;
  }
  button {
    margin: 1rem;
    display: block;
    margin-right: auto;
    margin-left: auto;
    border-radius: 5px;
    border-color: black;
    border-width: 2px;
    background-color: white;
  }
</style>
