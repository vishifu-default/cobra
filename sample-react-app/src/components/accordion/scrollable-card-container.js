import { Column, Grid } from '@carbon/react';
import CardItem from './card-item';

function ScrollableCardContainer() {
  const sample = [
    { id: 1, name: 'test 1', imageUrl: 'https://my-cobra.s3.ap-southeast-1.amazonaws.com/posters/v1-poster.png' },
    { id: 2, name: 'test 1', imageUrl: 'https://my-cobra.s3.ap-southeast-1.amazonaws.com/posters/v1-poster.png' },
    { id: 3, name: 'test 1', imageUrl: 'https://my-cobra.s3.ap-southeast-1.amazonaws.com/posters/v1-poster.png' },
    { id: 4, name: 'test 1', imageUrl: 'https://my-cobra.s3.ap-southeast-1.amazonaws.com/posters/v1-poster.png' },
    { id: 5, name: 'test 1', imageUrl: 'https://my-cobra.s3.ap-southeast-1.amazonaws.com/posters/v1-poster.png' },
    { id: 6, name: 'test 1', imageUrl: 'https://my-cobra.s3.ap-southeast-1.amazonaws.com/posters/v1-poster.png' },
    { id: 7, name: 'test 1', imageUrl: 'https://my-cobra.s3.ap-southeast-1.amazonaws.com/posters/v1-poster.png' },
    { id: 8, name: 'test 1', imageUrl: 'https://my-cobra.s3.ap-southeast-1.amazonaws.com/posters/v1-poster.png' },
    { id: 9, name: 'test 1', imageUrl: 'https://my-cobra.s3.ap-southeast-1.amazonaws.com/posters/v1-poster.png' },
    { id: 10, name: 'test 1', imageUrl: 'https://my-cobra.s3.ap-southeast-1.amazonaws.com/posters/v1-poster.png' },
    { id: 11, name: 'test 1', imageUrl: 'https://my-cobra.s3.ap-southeast-1.amazonaws.com/posters/v1-poster.png' }
  ];

  return (
    <Grid fullWidth as={'div'} className={'accordion--grid-container'}>
      <>
        {sample.map((item) => (
          <Column key={item.id} span={4} style={{marginBottom: '1rem'}}>
            <CardItem id={item.id} displayName={item.name} imageUrl={item.imageUrl} />
          </Column>
        ))}
      </>
    </Grid>
  );
}

export default ScrollableCardContainer;
