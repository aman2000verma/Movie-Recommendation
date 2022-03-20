<script>
  import Table from "./ResultTable.svelte";
  export let userId = "";
  export let numMovies = "";
  export let engine = "";
  export let result;
  export let movies = [
    {
      movieID: 26375,
      title: "Silver Streak (1976)",
      genres: "Action|Comedy|Crime"
    },
    { movieID: 283, title: "New Jersey Drive (1995)", genres: "Crime|Drama" },
    {
      movieID: 228,
      title: "Destiny Turns on the Radio (1995)",
      genres: "Comedy"
    },
    { movieID: 184, title: "Nadja (1994)", genres: "Drama" },
    {
      movieID: 238,
      title: "Far From Home: The Adventures of Yellow Dog (1995)",
      genres: "Adventure|Children"
    },
    {
      movieID: 4,
      title: "Waiting to Exhale (1995)",
      genres: "Comedy|Drama|Romance"
    },
    { movieID: 61, title: "Eye for an Eye (1996)", genres: "Drama|Thriller" },
    { movieID: 132, title: "Jade (1995)", genres: "Thriller" },
    { movieID: 209, title: "White Man's Burden (1995)", genres: "Drama" },
    {
      movieID: 26729,
      title: "Hearts of Darkness: A Filmmakers Apocalypse (1991)",
      genres: "Documentary"
    }
  ];
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
