data = d3.range 5
  .map (n) -> (n + 1) * 5

w = 500
h = 100
padding = 5

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
  .attr 'x', (d, i) -> i * (w / data.length) + 20 * i
  .attr 'y', (d) -> h - d * 3
  .attr 'width', w / data.length - padding
  .attr 'height', (d) -> d * 3
  .attr 'stroke', 'purple'
  .attr 'stroke-width', (d) -> 10

s2.selectAll 'text'
  .data data
  .enter()
  .append 'text'
  .text (d) -> d
  .attr 'x', (d, i) -> i * (w / data.length) + (w / data.length) / 2 + 20 * i
  .attr 'y', (d) -> h - d
  .attr 'text-anchor', 'middle'
  .attr 'fill', 'white'

# Scatterplot
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
  .attr 'r', (d) -> Math.log2 d[1]
s3.selectAll 'text'
  .data s3data
  .enter()
  .append 'text'
  .text (d) -> d[0] + ',' + d[1]
  .attr 'x', (d) -> d[0]
  .attr 'y', (d) -> d[1]
  .attr 'fill', 'green'

# Scale
s4 = s()
data = []
getRandom = ->
  Math.round Math.random() * Math.random() * 1000
for i in [0 .. 10]
  data.push [getRandom(), getRandom()]
padding = 20
xMax = d3.max data, (d)-> d[0]
xMin = d3.min data, (d)-> d[0]
yMax = d3.max data, (d)-> d[1]
yMin = d3.min data, (d)-> d[1]
xScale = d3.scale.linear()
  .domain [xMin, xMax]
  .range [padding, w - padding]
  .nice()
yScale = d3.scale.linear()
  .domain [yMin, yMax]
  .range [h - padding, padding]
s4.selectAll 'circle'
  .data data
  .enter()
  .append 'circle'
  .attr 'cx', (d)-> xScale d[0]
  .attr 'cy', (d)-> yScale(d[1]) - padding
  .attr 'r', (d)-> Math.log2 d[1]
s4.selectAll 'text'
  .data data
  .enter()
  .append 'text'
  .attr 'x', (d)-> xScale d[0]
  .attr 'y', (d)-> yScale d[1]
  .attr 'fill', 'red'
# .text (d) -> "#{d[0]}, #{d[1]}"

# Axis
xAxis = d3.svg.axis()
  .scale xScale
  .orient 'bottom'
  .ticks 10
yAxis = d3.svg.axis()
  .scale yScale
  .orient 'left'
  .ticks 5

s4.append 'g'
  .attr 'class', 'axis'
  .call xAxis
  .attr 'transform',"translate(0, #{h - padding})"
s4.append 'g'
  .attr 'class', 'axis'
  .call yAxis
  .attr 'transform', "translate(30, 0)"

w = 600
h = 250
data = d3.range 10
s5 = s()
xScale = d3.scale.ordinal()
  .domain d3.range data.length
  .rangeRoundBands [0, w], 0.05
s5.selectAll 'rect'
  .data data
  .enter()
  .append 'rect'
  .transition()
  .attr 'x', (d, i)->xScale i
  .attr 'y', (d)-> h - d * 8
  .attr 'width', xScale.rangeBand()
  .attr 'height', (d)->d * 8
s5.selectAll 'rect'
  .on 'click', ->
    s5.selectAll 'rect'
      .data d3.range(10).map (d)->d * Math.random()
      .transition()
      .duration (d, i) -> i * 500
      .ease 'fade'
      .delay ->100
      .each 'start', ->
        d3.select this
          .attr 'fill', 'magenta'
      .attr 'x', (d, i)->xScale i
      .attr 'y', (d)-> h - d * 8
      .attr 'width', xScale.rangeBand()
      .attr 'height', (d)->d * 8
      .each 'end', ->
        d3.select this
          .attr 'fill', 'green'
          .transition()
          .duration 2000
          .attr 'fill', 'orange'
