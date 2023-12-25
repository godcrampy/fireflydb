-module(bitcask_test).
-export([main/0]).

main() ->
  % Add the path to the Bitcask ebin directory
  StartTime = erlang:system_time(microsecond),
  code:add_patha("_build/default/lib/bitcask/ebin"),
  ITERATIONS = 100000,

  % Open the Bitcask database
  DB = bitcask:open("db", [read_write]),

  % Run the write test
  {WriteDurations, Keys} = write_values(DB, ITERATIONS, [], []),

  % Run the read test
  {ReadDurations, _} = read_values(DB, Keys, ITERATIONS, []),

  % Run the read-write test
  {ReadLatencies, WriteLatencies, _} = read_write_alternate(DB, ITERATIONS, [], [], Keys),
  bitcask:close(DB),

  % Calculate and print results for write test
  AverageWriteDuration = calculate_average(WriteDurations),
  P90WriteDuration = calculate_percentile(WriteDurations, 90),
  io:format("Write Test Results:~n"),
  io:format("  Average write duration: ~p microseconds~n", [AverageWriteDuration]),
  io:format("  p90 write duration: ~p microseconds~n", [P90WriteDuration]),

  % Calculate and print results for read test
  AverageReadDuration = calculate_average(ReadDurations),
  P90ReadDuration = calculate_percentile(ReadDurations, 90),
  io:format("Read Test Results:~n"),
  io:format("  Average read duration: ~p microseconds~n", [AverageReadDuration]),
  io:format("  p90 read duration: ~p microseconds~n", [P90ReadDuration]),

  % Calculate and print results for the read and write alternate test
  AverageWriteLatency = calculate_average(WriteLatencies),
  P90WriteLatency = calculate_percentile(WriteLatencies, 90),
  io:format("Read Write Alternate Test Results:~n"),
  io:format("  Average write latency: ~p microseconds~n", [AverageWriteLatency]),
  io:format("  p90 write latency: ~p microseconds~n", [P90WriteLatency]),

  AverageReadLatency = calculate_average(ReadLatencies),
  P90ReadLatency = calculate_percentile(ReadLatencies, 90),
  io:format("  Average read latency: ~p microseconds~n", [AverageReadLatency]),
  io:format("  p90 read latency: ~p microseconds~n", [P90ReadLatency]),

  % Record the end time
  EndTime = erlang:system_time(microsecond),

  % Calculate and print the total runtime
  TotalDuration = (EndTime - StartTime) / 1000000,
  io:format("Total script runtime: ~p seconds~n", [TotalDuration]).

write_values(_, 0, Durations, Keys) ->
  {Durations, Keys};
write_values(DB, N, Durations, Keys) when N > 0 ->
  {Duration, UpdatedKeys} = write_operation(DB, Keys),

  UpdatedDurations = [Duration | Durations],

  write_values(DB, N - 1, UpdatedDurations, UpdatedKeys).

read_values(_, _, 0, Durations) ->
  {Durations, 0};
read_values(DB, Keys, N, Durations) when N > 0 ->
  {Duration, _} = read_operation(DB, Keys),

  UpdatedDurations = [Duration | Durations],

  read_values(DB, Keys, N - 1, UpdatedDurations).


% Helper function for the read and write alternate test
read_write_alternate(_, 0, ReadLatencies, WriteLatencies ,Keys) ->
  {ReadLatencies, WriteLatencies, Keys};
read_write_alternate(DB, N, ReadLatencies, WriteLatencies, Keys) when N > 0 ->
  % Write operation
  {WriteLatency, UpdatedKeys} = write_operation(DB, Keys),

  % Read operation
  {ReadLatency, _} = read_operation(DB, Keys),

  % Update latencies lists
  UpdatedReadLatencies = [ReadLatency | ReadLatencies],
  UpdatedWriteLatencies = [WriteLatency | WriteLatencies],

  % Continue with the next iteration
  read_write_alternate(DB, N - 1, UpdatedReadLatencies, UpdatedWriteLatencies, UpdatedKeys).


% Helper function for the write operation
write_operation(DB, Keys) ->
  Key = generate_random_binary(8),
  Value = generate_random_binary(100),

  StartTime = erlang:system_time(microsecond),
  ok = bitcask:put(DB, Key, Value),
  EndTime = erlang:system_time(microsecond),
  Duration = EndTime - StartTime,

  UpdatedKeys = [Key | Keys],

  {Duration, UpdatedKeys}.

% Helper function for the read operation
read_operation(DB, Keys) ->
  RandomKey = lists:nth(rand:uniform(length(Keys)), Keys),

  StartTime = erlang:system_time(microsecond),
  _ = bitcask:get(DB, RandomKey),
  EndTime = erlang:system_time(microsecond),
  Duration = EndTime - StartTime,

  {Duration, Keys}.

generate_random_binary(Length) ->
  crypto:strong_rand_bytes(Length).

calculate_average(List) ->
  case lists:sum(List) of
    0 -> 0;
    Sum -> Sum / length(List)
  end.

calculate_percentile(List, Percentile) when Percentile > 0, Percentile =< 100 ->
  SortedList = lists:sort(List),
  Length = length(SortedList),
  Index = trunc((Percentile / 100) * (Length - 1)) + 1,
  lists:nth(Index, SortedList).
