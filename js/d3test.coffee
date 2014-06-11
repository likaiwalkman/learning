data = d3.range 5
  .map (n) -> (n + 1) * 5

w = 500
h = 100
padding = 1

s = ->
  d3.select 'body'
  .append 'svg'
  .attr 'width', "#{w}px"
  .attr 'height', "#{h}px"

s().selectAll 'circle'
  .data data
  .enter()
  .append 'circle'
  .attr 'cx', (d, i) -> i * 50 + 25
  .attr 'cy', h/2
  .attr 'r', (d) -> d
  .attr 'fill', 'yellow'
  .attr 'stroke', 'orange'
  .attr 'stroke-width', (d) -> d/2

s2 = s()
s2.selectAll 'rect'
  .data data
  .enter()
  .append 'rect'
  .attr 'fill', 'teal'
  .attr 'x', (d, i) -> i * (w / data.length)
  .attr 'y', (d) -> h - d
  .attr 'width', w / data.length - padding
  .attr 'height', (d) -> d

s2.selectAll 'text'
  .data data
  .enter()
  .append 'text'
  .text (d) -> d
  .attr 'x', (d, i) -> i * (w / data.length) + (w / data.length) / 2
  .attr 'y', (d) -> h - d - 3
  .attr 'text-anchor', 'middle'

s3data = [
  [5, 20]
  [480, 90]
  [250, 50]
  [100, 33]
  [330, 95]
  [410, 12]
  [475, 44]
  [25, 67]
  [85, 21]
  [220, 88]
]
s3 = s()
s3.selectAll 'circle'
  .data s3data
  .enter()
  .append 'circle'
  .attr 'cx', (d) -> d[0]
  .attr 'cy', (d) -> d[1]
  .attr 'r', 5
s3.selectAll 'text'
  .data s3data
  .enter()
  .append 'text'
  .text (d) -> d[0] + ',' + d[1]
  .attr 'x', (d) -> d[0]
  .attr 'y', (d) -> d[1]
  .attr 'fill', 'green'
