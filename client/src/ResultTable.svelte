<script>
  export let movies;
  export let users;
</script>

<div>
  {#if movies}
    <table>
      <thead>
        <tr><h4>Recommended Movies</h4></tr>
        <tr>
          <td>Movie ID</td>
          <td>Movie Name</td>
          <td>Genres</td>
        </tr>
      </thead>
      {#each movies as movie}
        <tr>
          <td>{movie.movieID}</td>
          <td>{movie.title}</td>
          <td>{movie.genres}</td>
        </tr>
      {/each}
    </table>
  {/if}

  {#if users}
    <table>
      <thead>
        <tr><h4>Top 10 Similar Users</h4></tr>
        <tr>
          <td>User ID</td>
          <td>Gender</td>
          <td>Age</td>
          <td>Occupation</td>
          <td>Zipcode</td>
        </tr>
      </thead>
      {#each users as user}
        <tr>
          <td>{user.userID}</td>
          <td>{user.sex}</td>
          <td>{user.age}</td>
          <td>{user.occupation}</td>
          <td>{user.zipcode}</td>
        </tr>
      {/each}
    </table>
  {/if}
</div>

<style>
  div {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: space-evenly;
    margin-left: auto;
    margin-right: auto;
    margin-bottom: 2%;
  }
  table,
  thead,
  td {
    border: 1px solid;
    align-items: center;
    color: black;
    border-collapse: collapse;
    text-align: start;
  }

  table {
    margin-bottom: 2%;
  }
</style>
